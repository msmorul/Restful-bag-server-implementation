/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.pkg;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.regex.Matcher;
import org.chronopolis.ingest.Util;

/**
 *
 * @author toaster
 */
public class BagBuildListener extends ManifestBuildListener.Adapter {

    private OutputStream baseStream;
    private ChronPackage pkg;
    private boolean holey = false;
    private BagWriter writer;
    private String urlPattern;
    private boolean closeOutput = false;
    private long totalBytes = 0;
    private long totalFiles = 0;

    public BagBuildListener(ChronPackage pkg, OutputStream bagStream, boolean holey) {
        this.holey = holey;
        this.pkg = pkg;
        
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
        this.urlPattern = urlPatter;
    }

    @Override
    public void startBuild(ManifestBuilder builder) {
        writer = new BagWriter(baseStream, pkg);
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
            pkg = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startItem(ManifestBuilder builder, long size, String item) {
        if (holey) {
            String replacementPkg = Matcher.quoteReplacement(pkg.getName());
            String rawpath = Matcher.quoteReplacement(item);
            String datapath = "data/" + rawpath;

            String url = urlPattern.replaceAll("\\{b\\}", replacementPkg).replaceAll("\\{d\\}", datapath).replaceAll("\\{r\\}", rawpath);
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
