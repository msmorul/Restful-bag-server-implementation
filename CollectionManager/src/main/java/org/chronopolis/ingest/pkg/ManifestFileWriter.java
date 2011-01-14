/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.pkg;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * listener for writing a manifest out to the supplied outputstream.
 * This will NOT close the outputstream when finished writing.
 * 
 * @author toaster
 */
public class ManifestFileWriter extends ManifestBuildListener.Adapter {

    private PrintWriter writer;

    public ManifestFileWriter(OutputStream os) {
        writer = new PrintWriter(os, true);
    }

    @Override
    public void endItem(ManifestBuilder builder, String item, String digest) {
        writer.println(digest + "  " + item);
    }

    @Override
    public void endBuild(ManifestBuilder builder) {
        writer.flush();
    }
}
