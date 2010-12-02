/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.pkg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author toaster
 */
public class DelayedTransferStream extends OutputStream {

    private URL outputURL;
    private OutputStream tmpOS;
    private File scratch;
    private int responseCode = -1;
    private String responseMessage = null;

    public DelayedTransferStream(URL outputURL) throws IOException {
        this.outputURL = outputURL;
        scratch = File.createTempFile("bag", "tmp");
        tmpOS = new FileOutputStream(scratch);
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    @Override
    public void write(int b) throws IOException {
        tmpOS.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        tmpOS.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        tmpOS.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        tmpOS.flush();
    }

    @Override
    public void close() throws IOException {
        if (tmpOS == null) {
            return;
        }

        tmpOS.close();
        tmpOS = null;

        HttpURLConnection connection;
        FileInputStream fis = new FileInputStream(scratch);

        connection = (HttpURLConnection) outputURL.openConnection();
        connection.setChunkedStreamingMode(32768);
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        OutputStream os;
        try {
            os = new GZIPOutputStream(connection.getOutputStream());
        } catch (IOException ioe) {
            responseMessage = ioe.getMessage();
            return;
        }
        byte[] block = new byte[32768];
        int read = 0;
        while ((read = fis.read(block)) != -1) {
            os.write(block, 0, read);
        }

        fis.close();
        os.close();
        scratch.delete();
        responseCode = connection.getResponseCode();
        responseMessage = connection.getResponseMessage();
        connection.disconnect();
    }
}
