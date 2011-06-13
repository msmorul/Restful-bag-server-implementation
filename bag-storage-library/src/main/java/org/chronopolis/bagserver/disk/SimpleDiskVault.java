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
package org.chronopolis.bagserver.disk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import org.chronopolis.bagserver.BagEntry;
import org.chronopolis.bagserver.BagInfo;
import org.chronopolis.bagserver.BagIt;
import org.chronopolis.bagserver.BagVault;

/**
 * Simple disk bag vault which assumes all bags exist in a flat hierarchy. Bags
 * are stored on disk in two directories
 * work - bags which are allowed to be modified
 * committed - static, unchanging bags.
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
                return false;
            }

            return pathname.isDirectory();
        }
    };
    private FileFilter manifestFilter = new FileFilter() {

        public boolean accept(File pathname) {
            return pathname.isFile() && pathname.getName().matches("^manifest-[a-z0-9]+\\.txt$");
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
        if (identifier == null) {
            throw new IllegalArgumentException("Null identifier");
        }
        File f = new File(workDirectory, identifier);
        if (!f.getParentFile().equals(workDirectory)) {
            throw new IllegalArgumentException("Bad directory name " + identifier);
        }
        return f;
    }

    private File getCommitedBag(String identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("Null identifier");
        }
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

            if (!workingBag.mkdir() || !new File(workingBag, "data").mkdir()) {
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

        

        public List<String> listDataFiles() {
            List<String> retList = new ArrayList<String>();

            int len = directory.getAbsolutePath().length() + 1;

            Queue<File> dirList = new LinkedList<File>();
            dirList.offer(new File(directory, "data"));

            File current;
            while ((current = dirList.poll()) != null) {
                for (File f : current.listFiles()) {
                    if (f.isDirectory()) {
                        dirList.offer(f);
                    } else {
                        retList.add(f.getAbsolutePath().substring(len));
                    }
                }
            }

            return retList;
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

        public List<String> listTagFiles() {
            List<String> retList = new ArrayList<String>();
            for (File f : directory.listFiles())
            {
                if (f.isFile())
                    retList.add(f.getName());

            }
            return retList;
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

        public boolean setBagItInformation(BagIt bagIt) {
            File f = new File(directory, BagIt.FILE_NAME);
            try {
                FileWriter fw = new FileWriter(f);
                bagIt.writeFile(fw);
                fw.close();
                return true;
            } catch (IOException e) {
                LOG.error("Cannot write bagit.txt", e);
                return false;
            }
        }

        public boolean setBagInfo(BagInfo baginfo) {
            File f = new File(directory, BagInfo.FILE_NAME);
            try {
                FileWriter fw = new FileWriter(f);
                baginfo.writeInfo(fw);
                fw.close();
                return true;
            } catch (IOException e) {
                LOG.error("Cannot write bag-info.txt", e);
                return false;
            }
        }

        public BagIt getBagIt() {
            File f = new File(directory, BagIt.FILE_NAME);
            if (f.isFile()) {
                try {
                    return BagIt.readFile(new FileReader(f));
                } catch (IOException e) {
                    LOG.error("Error reading: " + f, e);
                    return null;
                }

            }
            return null;
        }

        public BagInfo getBagInfo() {
            File f = new File(directory, BagInfo.FILE_NAME);
            if (f.isFile()) {
                try {
                    return BagInfo.readInfo(new FileReader(f));
                } catch (IOException e) {
                    LOG.error("Error reading: " + f, e);
                    return null;
                }
            }
            return null;
        }

        public InputStream openTagInputStream(String tagItem) throws IllegalArgumentException {
            File dataFile = new File(directory, tagItem);
            if (!dataFile.getParentFile().equals(directory)) {
                throw new IllegalArgumentException("Bag tag item " + tagItem);
            }
            try {
                return new FileInputStream(dataFile);
            } catch (FileNotFoundException e) {
                LOG.error("Cannot open file: " + dataFile + " for id: " + tagItem);
                return null;
            }
        }

        public OutputStream openTagOutputStream(String tagItem) throws IllegalArgumentException {


            File dataFile = new File(directory, tagItem);
            if (!dataFile.getParentFile().equals(directory)) {
                throw new IllegalArgumentException("Bag tag item " + tagItem);
            }
            try {
                return new FileOutputStream(dataFile);
            } catch (FileNotFoundException e) {
                LOG.error("Cannot create output file: " + dataFile + " for id: " + tagItem);
                return null;
            }
        }

        public InputStream openDataInputStream(String fileIdentifier) throws IllegalArgumentException {
            File dataFile = new File(directory, "data/" + fileIdentifier);

            //TODO: check to make sure abs path it in data dir;
            try {
                return new FileInputStream(dataFile);
            } catch (FileNotFoundException e) {
                LOG.error("Cannot open file for reading: " + dataFile + " for id: " + fileIdentifier);
                return null;
            }
        }

        public OutputStream openDataOutputStream(String fileIdentifier) throws IllegalArgumentException {
            File dataFile = new File(directory, "data/" + fileIdentifier);

            //TODO: check to make sure abs path it in data dir;
            if (!dataFile.getParentFile().exists()) {
                if (!dataFile.getParentFile().mkdirs()) {
                    throw new IllegalArgumentException("Cannot create parent dir " + dataFile.getParentFile());
                }
            }
            try {
                return new FileOutputStream(dataFile);
            } catch (FileNotFoundException e) {
                LOG.error("Cannot create output file: " + dataFile + " for id: " + fileIdentifier);
                return null;
            }
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
