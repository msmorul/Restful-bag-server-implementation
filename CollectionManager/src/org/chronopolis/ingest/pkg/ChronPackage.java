/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.pkg;

import java.io.File;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;

/**
 *
 * @author toaster
 */
public class ChronPackage {

    private String name = "";
    private String digest = "SHA-256";
    private List<File> rootList = new ArrayList();
    private List<ChronPackageListener> listeners = new ArrayList<ChronPackageListener>();
    private Map<String, String> metadataMap = new HashMap<String, String>();
    private boolean readOnly = false;

    public String getName() {
        return name;
    }

    public void setReadOnly(boolean readOnly) {
        boolean old = this.readOnly;
        this.readOnly = readOnly;
        for (ChronPackageListener l : listeners) {
            l.readOnlyChanged(this, old);
        }
    }

    public Statistics createStatistics(AbortScanNotifier notifier) {
        Statistics stats = new Statistics();
        for (File f : rootList) {
            updateStats(f, stats, notifier);
        }
        if (notifier.aborted()) {
            return null;
        } else {
            return stats;
        }
    }

    private void updateStats(File f, Statistics stats, AbortScanNotifier notifier) {
        if (!f.canRead())
        {
                stats.unreadable++;
                stats.unreadableFiles.add(f);
                return;
        }
        
        if (f.isFile()) {
            stats.files++;
            stats.size += f.length();
        } else if (f.isDirectory()) {
            stats.directories++;
            for (File f2 : f.listFiles()) {
                if (notifier.aborted()) {
                    return;
                }
                updateStats(f2, stats, notifier);
            }
        } else {
            //errors here
        }
    }

    /**
     * Return the first file to be written in this bag.
     * 
     * @return first data file (ie, data/somedir/file4.txt)
     */
    public String findRelativeFirstFile() {
        if (rootList.isEmpty()) {
            return null;
        }
        for (File f : rootList) {
            if (f.exists()) {
                File first = trollForFirst(f);
//                System.out.println("f.getAbsolutePath().length()+1 " +(f.getAbsolutePath().length()+1) + " " + first.getAbsolutePath().substring(f.getAbsolutePath().length()+1));
                return first.getAbsolutePath().substring(f.getAbsolutePath().length()+1);
            }
        }
        return null;
    }

    public File findFirstFile() {
        if (rootList.isEmpty()) {
            return null;
        }
        for (File f : rootList) {
            if (f.exists()) {
                return trollForFirst(f);
            }
        }
        return null;
    }

    private File trollForFirst(File dir) {

        File firstDir = null;

        for (File f : dir.listFiles()) {

            if (f.isFile()) {
//                return dir.getName() + "/" + f.getName();
                return f;
            } else if (firstDir == null && f.isDirectory()) {
                firstDir = f;
            }
        }
        if (firstDir == null) {
            //return dir.getName();
            return dir;
        }

//        return dir.getName() + "/" + trollForFirst(firstDir);
        return trollForFirst(firstDir);
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public String getDigest() {
        return digest;
    }

    public String getBagFormattedDigest() {
        return digest.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    public void setDigest(String digest) {
        String old = this.digest;
        this.digest = digest;
        for (ChronPackageListener l : listeners) {
            l.digestChanged(this, old);
        }
    }

    public void setName(String name) {
        String old = this.name;
        this.name = name;
        for (ChronPackageListener p : listeners) {
            p.nameChanged(this, old);
        }
    }

    public Map<String, String> getMetadataMap() {
        return metadataMap;
    }

    public List<File> getRootList() {
        return rootList;
    }

    public List<ChronPackageListener> getChronPackageListeners() {
        return listeners;
    }

    public class Statistics {

        private long size = 0;
        private long files = 0;
        private long directories = 0;
        private long unreadable = 0;
        private List<File> unreadableFiles = new ArrayList<File>();

        public long getDirectories() {
            return directories;
        }

        public long getFiles() {
            return files;
        }

        public long getSize() {
            return size;
        }

        public long getUnreadable() {
            return unreadable;
        }

        public List<File> getUnreadableFiles() {
            return unreadableFiles;
        }



    }

    public static interface AbortScanNotifier {

        public boolean aborted();
    }
}
