/*
 * Copyright (c) 2007-2011, University of Maryland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the University of Maryland nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ACE Components were written in the ADAPT Project at the University of
 * Maryland Institute for Advanced Computer Study.
 */
package org.chronopolis.ingest.pkg;

import com.sun.jersey.api.representation.Form;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
//import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.apache.log4j.Logger;
import org.apache.pivot.collections.Map;
import org.chronopolis.ingest.Util;

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
    private long totalFiles = 0;
    private OutputStream dataPutStream = null;
    private boolean holey;
    private UrlFormatter formatter;
    private String pkgId;
    private HttpURLConnection uc;

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
        totalFiles++;
        try {
            if (!holey) {

                URL putURI = new URL(URLUTF8Encoder.encode(baseURL.toASCIIString() + rel));
                LOG.debug("Opening data connection to: " + putURI);

                uc = (HttpURLConnection) putURI.openConnection();
                uc.setDoOutput(true);
                uc.setRequestMethod("PUT");
                HttpURLConnection.setFollowRedirects(true);
                uc.setChunkedStreamingMode(32768);
                dataPutStream = uc.getOutputStream();
            } else {
                uc = null;
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
                LOG.debug("Closing file put: " + item);
                dataPutStream.close();
                if (uc.getResponseCode() != 200) {
                    LOG.error("Unexpected result code: " + uc.getResponseCode());
                    throw new RuntimeException("Unexpected result code: " + uc.getResponseCode());
                }
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
        LOG.debug("Sendinf bagit, info, manifest and fetch ");
        // TODO: upload bagit, info, manifest, fetch files
        writeVersion();
        writeInfo(builder);
        try {
            writeList("manifest-" + builder.getPackage().getBagFormattedDigest() + ".txt", digestList);
            writeList("fetch.txt", fetchList);
        } catch (IOException e) {
            LOG.error("Error uploading fetch or manifest: ", e);
            throw new RuntimeException(e);
        }

        LOG.debug("POSTing commit instruction ");
        Form f = new Form();
        f.add("commit", "true");
        Client c = Client.create();
        WebResource r = c.resource(URI.create(baseURL.toString() + "/" + pkgId));
        LOG.debug("POSTing commit instruction " + r.getURI());
        r.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        try {
            r.post(f);
        } catch (UniformInterfaceException e) {
            LOG.error(e.getResponse());
            builder.cancel();
        }
    }

    private void writeList(String name, List<String> list) throws IOException {
        if (list.isEmpty()) {
            return;
        }
        File tmpFile = File.createTempFile("list", ".txt");
        PrintWriter writer = new PrintWriter(new FileWriter(tmpFile));
        for (String s : list) {
            writer.println(s);
        }
        writer.close();

        String rel = "/" + pkgId + "/contents/" + name;

        URL putURI = new URL(baseURL.toASCIIString() + rel);
        LOG.debug("Sending metadata to: " + putURI);
        HttpURLConnection uc = (HttpURLConnection) putURI.openConnection();
        uc.setDoOutput(true);
        uc.setRequestMethod("PUT");
        HttpURLConnection.setFollowRedirects(true);
        uc.setChunkedStreamingMode(32768);
        OutputStream os = uc.getOutputStream();


        FileInputStream fis = new FileInputStream(tmpFile);
        byte[] block = new byte[32786];
        int read;
        while ((read = fis.read(block)) != -1) {
            os.write(block, 0, read);
        }
        fis.close();
        os.close();

    }

    private void writeInfo(ManifestBuilder builder) {
        Map<String, String> metadata = builder.getPackage().getMetadataMap();
        metadata.put(BagWriter.INFO_BAGGING_DATE, BagWriter.DATE_FORMAT.format(new Date()));
        metadata.put(BagWriter.INFO_PAYLOAD_OXUM, totalBytes + "." + totalFiles);
        metadata.put(BagWriter.INFO_BAG_SIZE, Util.formatSize(totalBytes));


        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            for (String key : metadata) {
                pw.println(key + ": " + metadata.get(key));
            }

            byte[] block = sw.toString().getBytes("UTF-8");

            String rel = "/" + pkgId + "/contents/bag-info.txt";

            URL putURI = new URL(baseURL.toASCIIString() + rel);
            LOG.debug("Sending bag-info to: " + putURI);
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

    private void writeVersion() {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println("BagIt-Version: 0.96");
            pw.println("Tag-File-Character-Encoding: UTF-8");
            byte[] block = sw.toString().getBytes("UTF-8");
            String rel = "/" + pkgId + "/contents/bagit.txt";
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
