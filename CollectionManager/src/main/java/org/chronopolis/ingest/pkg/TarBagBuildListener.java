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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import org.chronopolis.ingest.Util;

/**
 *
 * @author toaster
 */
public class TarBagBuildListener extends ManifestBuildListener.Adapter {

    private OutputStream baseStream;
    private boolean holey = false;
    private BagWriter writer;
    private UrlFormatter formatter;
    private boolean closeOutput = false;
    private long totalBytes = 0;
    private long totalFiles = 0;

    public TarBagBuildListener(OutputStream bagStream, boolean holey) {
        this.holey = holey;
        
        this.baseStream = bagStream;
    }

    public boolean isCloseOutput() {
        return closeOutput;
    }

    public void setCloseOutput(boolean closeOutput) {
        this.closeOutput = closeOutput;
    }

    public boolean isHoley() {
        return holey;
    }

    public void setUrlPattern(String urlPatter) {
        this.formatter = new UrlFormatter(urlPatter);
    }

    @Override
    public void startBuild(ManifestBuilder builder) {
        writer = new BagWriter(baseStream, builder.getPackage());
        writer.addMetadata(BagWriter.INFO_BAGGING_DATE, BagWriter.DATE_FORMAT.format(new Date()));
    }

    @Override
    public void endItems(ManifestBuilder builder) {
        writer.addMetadata(BagWriter.INFO_PAYLOAD_OXUM, totalBytes + "."+totalFiles);
        writer.addMetadata(BagWriter.INFO_BAG_SIZE, Util.formatSize(totalBytes) );
    }

    @Override
    public void endBuild(ManifestBuilder builder) {
        try {
            writer.finish();
            if (closeOutput) {
                baseStream.close();
            }
            baseStream = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startItem(ManifestBuilder builder, long size, String item) {
        if (holey) {
            String url = formatter.format(item);
            writer.addFetchEntry(url, size, "data/" + item);
        } else {
            try {

                writer.openEntry("data/" + item, size);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void endItem(ManifestBuilder builder, String item, String digest) {
        try {
            writer.addDigestEntry("data/" + item, digest);
            if (!holey) {
                writer.closeEntry();
            }
            totalFiles++;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeBytes(ManifestBuilder builder, byte[] block, int offset, int length) {
        try {
            if (!holey) {
                writer.writeContent(block, offset, length);
            }
            totalBytes += length;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
