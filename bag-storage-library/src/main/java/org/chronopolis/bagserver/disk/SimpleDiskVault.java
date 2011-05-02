/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.bagserver.disk;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import org.chronopolis.bagserver.BagEntry;
import org.chronopolis.bagserver.BagInfo;
import org.chronopolis.bagserver.BagIt;
import org.chronopolis.bagserver.BagVault;

/**
 * Simple disk bag vault which assumes all bags exist in a flat hierarchy. 
 * 
 * @author toaster
 */
public class SimpleDiskVault implements BagVault {

    private static final Logger LOG = Logger.getLogger(SimpleDiskVault.class);
    private Lock creationLock = new ReentrantLock();
    private File commitDirectory;
    private File workDirectory;
    private File logDirectory;
    private FileFilter bagFilter = new FileFilter() {

        public boolean accept(File pathname) {
            if (pathname.getName().startsWith("_")) {
                return true;
            }

            return pathname.isDirectory();
        }
    };

    /**
     *
     * @param identifier
     * @return
     */
    public BagEntry getBag(String identifier) {
        File commit = getCommitedBag(identifier);
        File workb = getWorkingBag(identifier);
        if (commit.exists()) {
            return new MyBagEntry(commit);
        } else if (workb.exists()) {
            return new MyBagEntry(workb);
        }
        return null;
    }

    public SimpleDiskVault(File baseDirectory) {

        this.commitDirectory = new File(baseDirectory, "commited");
        if (!commitDirectory.isDirectory()) {
            if (!commitDirectory.mkdir()) {
                throw new IllegalStateException("Commit directory not writable " + commitDirectory);
            }
        }
        this.workDirectory = new File(baseDirectory, "work");
        if (!workDirectory.isDirectory()) {
            if (!workDirectory.mkdir()) {
                throw new IllegalStateException("Work directory not writable " + workDirectory);
            }
        }
        this.logDirectory = new File(baseDirectory, "logs");
        if (!logDirectory.isDirectory()) {
            if (!logDirectory.mkdir()) {
                throw new IllegalStateException("Log directory not writable " + logDirectory);
            }
        }
    }

    public List<BagEntry> getBags() {
        List<BagEntry> bags = new ArrayList<BagEntry>();

        for (File f : commitDirectory.listFiles(bagFilter)) {
            bags.add(new MyBagEntry(f));
        }
        for (File f : workDirectory.listFiles(bagFilter)) {
            bags.add(new MyBagEntry(f));
        }
        return bags;
    }

    /**
     * 
     * @param identifier
     * @return
     */
    private File getWorkingBag(String identifier) {
        File f = new File(workDirectory, identifier);
        if (!f.getParentFile().equals(workDirectory)) {
            throw new IllegalArgumentException("Bad directory name " + identifier);
        }
        return f;
    }

    private File getCommitedBag(String identifier) {
        File f = new File(commitDirectory, identifier);
        if (!f.getParentFile().equals(commitDirectory)) {
            throw new IllegalArgumentException("Bad directory name " + identifier);
        }
        return f;
    }

    private boolean clearDirectory(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                if (!f.delete()) {
                    LOG.error("Cannot delete " + f);
                    return false;
                }
            }
            if (f.isDirectory()) {
                if (!clearDirectory(f)) {
                    LOG.error("Cannot delete " + f);
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public boolean bagExists(String identifier) {
        File commitBag = getCommitedBag(identifier);
        File workingBag = getWorkingBag(identifier);
        return commitBag.isDirectory() || workingBag.isDirectory();
    }

    public BagEntry createNewBag(String newIdentifier) {
        LOG.trace("Request to create new bag: " + newIdentifier);
        creationLock.lock();
        try {
            File commitBag = getCommitedBag(newIdentifier);
            File workingBag = getWorkingBag(newIdentifier);

            if (commitBag.exists()) {
                LOG.info("Attempt to create existing/commited bag " + newIdentifier);
                throw new IllegalStateException("Attempt to create existing/commited bag ");
            }

            if (workingBag.exists()) {
                LOG.info("Attempt to create existing/working bag " + newIdentifier);
                return new MyBagEntry(workingBag);
            }

            if (!workingBag.mkdir()) {
                LOG.error("Count not create bag directory " + workingBag);
                throw new RuntimeException("Could not create bag " + workingBag);
            }
            return new MyBagEntry(workingBag);

        } finally {
            creationLock.unlock();
        }

    }

    private class MyBagEntry implements BagEntry {

        private File directory;

        public MyBagEntry(File directory) {
            this.directory = directory;
        }

        public String getIdentifier() {
            return directory.getName();
        }

        public State getBagState() {
            if (!directory.exists() || directory.getName().startsWith("_")) {
                return State.NONEXISTENT;
            }
            if (directory.getParentFile().equals(commitDirectory)) {
                return State.COMMITTED;
            } else if (directory.getParentFile().equals(workDirectory)) {
                return State.OPEN;
            }
            throw new IllegalStateException("Cannot determine bag state " + directory);
        }

        public boolean commit() {
            creationLock.lock();
            try {
                if (getBagState() == State.COMMITTED) {
                    return true;
                } else if (getBagState() == State.NONEXISTENT) {
                    throw new IllegalStateException("Cannot commit nonexistent bag");
                }

                File newLocation = getCommitedBag(directory.getName());
                if (newLocation.exists()) {
                    LOG.error("Commit directory for " + directory.getName() + " exists " + newLocation);
                    return false;
                }

                if (directory.renameTo(newLocation)) {
                    directory = newLocation;
                    LOG.info("Successfully committed " + directory.getName());
                    return true;
                }

                LOG.error("Count not rename " + directory + " to " + newLocation);

                return false;

            } finally {
                creationLock.unlock();
            }
        }

        public boolean delete() {

            //TODO: check for open files

            File deleteFile;
            creationLock.lock();
            try {
                if (getBagState() != State.NONEXISTENT) {
                    deleteFile = new File(directory.getParent(), "_" + directory.getName());
                    if (!directory.renameTo(deleteFile)) {
                        LOG.error("Could not rename directory for deletion" + directory + " " + deleteFile);
                        return false;
                    }
                } else {
                    return true;
                }

            } finally {
                creationLock.unlock();
            }

            return clearDirectory(deleteFile);
        }

        public void setBagItInformation(BagIt bagIt) {
        }

        public void setBagInfo(BagInfo baginfo) {
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + (this.directory != null ? this.directory.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MyBagEntry other = (MyBagEntry) obj;
            if (this.directory != other.directory && (this.directory == null || !this.directory.getName().equals(other.directory.getName()))) {
                return false;
            }
            return true;
        }
    }
}
