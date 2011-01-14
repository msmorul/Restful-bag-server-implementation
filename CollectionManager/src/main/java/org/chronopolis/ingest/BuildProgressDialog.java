/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest;

import java.io.IOException;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Meter;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;
import org.chronopolis.ingest.pkg.ManifestBuildListener;
import org.chronopolis.ingest.pkg.ManifestBuilder;

/**
 *
 * @author toaster
 */
public class BuildProgressDialog extends Sheet {

    @WTKX
    private Label fileLabel;
    @WTKX
    private Label totalLabel;
    @WTKX
    private Meter progressMeter;
    @WTKX
    private Meter fileMeter;
    private ManifestBuilder builder;
    private BuildListener listener = new BuildListener();

    public BuildProgressDialog() {

        try {

            WTKXSerializer serializer = new WTKXSerializer();
            Component mainW = (Component) serializer.readObject(this, "buildProgressDialog.wtkx");
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
