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
package org.chronopolis.ingest;

import java.io.IOException;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Meter;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.Window;
import org.chronopolis.ingest.pkg.ManifestBuildListener;
import org.chronopolis.ingest.pkg.ManifestBuilder;

/**
 *
 * @author toaster
 */
public class BuildProgressDialog extends Sheet {

    @BXML
    private Label fileLabel;
    @BXML
    private Label totalLabel;
    @BXML
    private Meter progressMeter;
    @BXML
    private Meter fileMeter;
    private ManifestBuilder builder;
    private BuildListener listener = new BuildListener();

    public BuildProgressDialog() {

        try {

            BXMLSerializer serializer = new BXMLSerializer();
            Component mainW = (Component) serializer.readObject(BuildProgressDialog.class, "buildProgressDialog.bxml");
            serializer.bind(this);
            setContent(mainW);

        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void open(Display display, Window owner, SheetCloseListener sheetCloseListener) {
        super.open(display, owner, sheetCloseListener);

        setPreferredWidth(owner.getWidth());

    }

    public void setBuilder(ManifestBuilder builder) {

        if (this.builder != null) {
            this.builder.getBuildListeners().remove(listener);
        }

        this.builder = builder;

        if (builder != null) {
            builder.getBuildListeners().add(listener);
            if (builder.getTotalSize() > 0) {
                progressMeter.setVisible(true);
            } else {
                progressMeter.setVisible(false);
            }
            listener.setTotalSize(builder.getTotalSize());
        }
    }

    private class BuildListener extends ManifestBuildListener.Adapter {

        private long totalSize = 0;
        private long totalSeen = 0;
        private long total = 0;
        private long fileSize = 0;
        private long seenSize = 0;
        private double lastPct = 0;

        public BuildListener() {
        }

        public void setTotalSize(long totalSize) {
            this.totalSize = totalSize;
        }

        @Override
        public void writeBytes(ManifestBuilder builder, byte[] block, int offset, int length) {
            seenSize += length;
            totalSeen += length;

            double filePct = (double) seenSize / fileSize;
            fileMeter.setPercentage(filePct);
            if (totalSize > 0) {
                double overallPct = (double) totalSeen / totalSize;
                progressMeter.setPercentage(overallPct);
                if ((overallPct - lastPct) > .02) {
                    lastPct = overallPct;
                    progressMeter.setText(Util.formatSize(totalSeen) + " / " + Util.formatSize(totalSize));
                }
            }
        }

        @Override
        public void startBuild(ManifestBuilder builder) {
            if (totalSize > 0) {
                progressMeter.setText("0 / " + Util.formatSize(totalSize));
            }
            total = 0;
        }

        @Override
        public void endBuild(ManifestBuilder builder) {
            close(builder.isExecute());
        }

        @Override
        public void endItem(ManifestBuilder builder, String item, String digest) {
            total++;
            totalLabel.setText(Long.toString(total));
        }

        @Override
        public void startItem(ManifestBuilder builder, long size, String item) {
            seenSize = 0;
            fileSize = size;
            fileMeter.setPercentage(0);
            fileLabel.setText(item);
        }
    }
}
