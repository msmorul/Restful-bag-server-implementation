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
