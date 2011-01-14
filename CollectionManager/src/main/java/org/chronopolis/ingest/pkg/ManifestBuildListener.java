/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.pkg;

import java.io.File;

/**
 *
 * @author toaster
 */
public interface ManifestBuildListener {

    public void startBuild(ManifestBuilder builder);

    public void buildException(ManifestBuilder builder);

    public void endBuild(ManifestBuilder builder);

    public void startItem(ManifestBuilder builder, long size, String item);

    public void endItem(ManifestBuilder builder, String item, String digest);
    public void startRoot(ManifestBuilder builder, File root);
    public void endRoot(ManifestBuilder builder, File root);
    public void writeBytes(ManifestBuilder builder, byte[] block, int offset, int length);
    public void endItems(ManifestBuilder builder);
    
    public static class Adapter implements ManifestBuildListener {

        public void startBuild(ManifestBuilder builder) {
        }

        public void buildException(ManifestBuilder builder) {
        }

        public void endBuild(ManifestBuilder builder) {
        }

        public void startItem(ManifestBuilder builder, long size, String item) {
        }

        public void endItem(ManifestBuilder builder, String item, String digest) {
        }

        public void startRoot(ManifestBuilder builder, File root) {
        }

        public void endRoot(ManifestBuilder builder, File root) {
        }

        public void writeBytes(ManifestBuilder builder, byte[] block, int offset, int length) {
        }

        public void endItems(ManifestBuilder builder) {
        }
        
    }
}
