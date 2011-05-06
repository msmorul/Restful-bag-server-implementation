/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.pkg;

import com.sun.jersey.api.representation.Form;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.apache.log4j.Logger;

/**
 *
 * @author toaster
 */
public class BagServerBuildListener extends ManifestBuildListener.Adapter {

    private URI baseURL;
    private static final Logger LOG = Logger.getLogger(BagServerBuildListener.class);
    private List<String> digestList = new ArrayList<String>();
    private List<String> fetchList = new ArrayList<String>(1000);
    private long totalBytes = 0;
    private OutputStream dataPutStream = null;
    private boolean holey;
    private UrlFormatter formatter;
    private String pkgId;

    public BagServerBuildListener(URI baseURL, boolean holey, String pkgId) {
        this.baseURL = baseURL;
        this.holey = holey;
        this.pkgId = pkgId;
    }

    public void setUrlPattern(String urlPatter) {
        this.formatter = new UrlFormatter(urlPatter);
    }

    /**
     * POST new bag to creation URL
     * @param builder
     */
    @Override
    public void startBuild(ManifestBuilder builder) {
        Form f = new Form();
        f.add("id", pkgId);
        Client c = Client.create();
        WebResource r = c.resource(baseURL);
        r.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        try {
            LOG.debug("POSTing creation to: " + baseURL);

            r.post(f);
        } catch (UniformInterfaceException e) {
            LOG.error(e.getResponse());
            builder.cancel();
            throw new RuntimeException("Server error: " + e.getResponse());
        }

    }

    @Override
    public void startItem(ManifestBuilder builder, long size, String item) {
        String rel = "/" + pkgId + "/contents/data/" + item;
        try {
            if (!holey) {
                URL putURI = new URL(baseURL.toASCIIString() + rel);
                LOG.debug("Opening data connection to: " + putURI);

                HttpURLConnection uc = (HttpURLConnection) putURI.openConnection();
                uc.setDoOutput(true);
                uc.setRequestMethod("PUT");
                HttpURLConnection.setFollowRedirects(true);
                uc.setChunkedStreamingMode(32768);
                dataPutStream = uc.getOutputStream();
            } else {
                String url = formatter.format(item);
                fetchList.add(url + "  " + size + "  data/" + item);
            }
        } catch (IOException e) {
            LOG.error("Error opening connection ", e);
            builder.cancel();
        }
    }

    @Override
    public void writeBytes(ManifestBuilder builder, byte[] block, int offset, int length) {
        totalBytes += length;
        if (dataPutStream != null) {
            try {
                dataPutStream.write(block, offset, length);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void endItem(ManifestBuilder builder, String item, String digest) {
        if (dataPutStream != null) {
            try {
                dataPutStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            dataPutStream = null;
        }
        digestList.add(digest + "  data/" + item);
    }

    /**
     * BUILD exception, remove server-side package
     *  - we should really look at just continuing here ;)
     * @param builder
     */
    @Override
    public void buildException(ManifestBuilder builder) {
        Form f = new Form();
        f.add("commit", "true");
        Client c = Client.create();
        WebResource r = c.resource(URI.create(baseURL.toString() + "/" + pkgId));
        r.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        try {
            r.delete();
        } catch (UniformInterfaceException e) {
            LOG.error(e.getResponse());
            builder.cancel();
        }
    }

    /**
     * 
     * @param builder
     */
    @Override
    public void endBuild(ManifestBuilder builder) {

        // TODO: upload bagit, info, manifest, fetch files
        writeVersion(builder);

        Client c = Client.create();
        WebResource r = c.resource(URI.create(baseURL.toString() + "/" + pkgId));
        r.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        try {
            r.post();
        } catch (UniformInterfaceException e) {
            LOG.error(e.getResponse());
            builder.cancel();
        }
    }

    private void writeVersion(ManifestBuilder builder) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println("BagIt-Version: 0.96");
            pw.println("Tag-File-Character-Encoding: UTF-8");
            byte[] block = sw.toString().getBytes("UTF-8");
            String rel = "/" + pkgId + "/contents";
            URL putURI = new URL(baseURL.toASCIIString() + rel);
            LOG.debug("Sending bagit to: " + putURI);
            HttpURLConnection uc = (HttpURLConnection) putURI.openConnection();
            uc.setDoOutput(true);
            uc.setRequestMethod("PUT");
            HttpURLConnection.setFollowRedirects(true);
            uc.setChunkedStreamingMode(32768);
            OutputStream os = uc.getOutputStream();
            os.write(block);
            os.close();
        } catch (IOException ex) {
            throw new RuntimeException("Could not write header", ex);
        }
    }
}
