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
package org.chronopolis.bagserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

/**
 *
 * @author toaster
 */
public final class BagChecker {

    private BagEntry workingBag;
    private static final Logger LOG = Logger.getLogger(BagChecker.class);
    private Pattern p = Pattern.compile("^manifest-[a-z0-9]+\\.txt$");
    private List<BagCheckListener> listenerList = new ArrayList<BagCheckListener>();
    private MyBagListener localListener;
    private boolean cancel = false;
    private byte[] block = new byte[32768];

    public BagChecker(BagEntry workingBag) {
        localListener = new MyBagListener();
        listenerList.add(localListener);
        this.workingBag = workingBag;
    }

    public void addListener(BagCheckListener listener) {
        listenerList.add(listener);
    }

    public void removeLIstener(BagCheckListener listener) {
        listenerList.remove(listener);
    }

    public List<String> getCorruptFiles() {
        return localListener.corruptFiles;
    }

    public List<String> getExtraFiles() {
        return localListener.extraFiles;
    }

//    public List<String> getManifestDifferences() {
//        return localListener.manifestDifferences;
//    }
    public List<String> getMissingFiles() {
        return localListener.missingFiles;
    }

    public BagEntry getBag() {
        return workingBag;
    }

    private void fireThreadTerminate() {
        for (BagCheckListener l : listenerList) {
            l.threadEnding(this);
        }
    }

    private void fireMissingBagIt() {
        for (BagCheckListener l : listenerList) {
            l.missingBagIt(this);
        }
    }

    private void fireMissingBagInfo() {
        for (BagCheckListener l : listenerList) {
            l.missingBagInfo(this);
        }
    }

    private void fireBadBagLine(String file, String line) {
        for (BagCheckListener l : listenerList) {
            l.badBagLine(this, file, line);
        }
    }

    private void fireMissingFile(String file) {
        for (BagCheckListener l : listenerList) {
            l.missingDigest(this, file);
        }
    }

    private void fireErrorReadingFile(String file, IOException e) {
        for (BagCheckListener l : listenerList) {
            l.errorReadingFile(this, file, e);
        }
    }

    private void fireMissingDigest(String file) {
        for (BagCheckListener l : listenerList) {
            l.missingDigest(this, file);
        }
    }

    private void fireCorruptFile(String file, String expected, String seen) {
        for (BagCheckListener l : listenerList) {
            l.corruptFile(this, file, expected, seen);
        }
    }

    public void cancel() {
        cancel = true;
    }

    public void validate(boolean fork) {
        cancel = false;
        if (fork) {
            Runnable r = new Runnable() {

                public void run() {
                    wrapValidate();
                }
            };
            Thread t = new Thread(r);
            t.start();


        } else {
            wrapValidate();
        }
    }

    private void wrapValidate() {
        try {
            validateBag();
        } finally {
            fireThreadTerminate();
        }
    }

    private void validateBag() {
        localListener.reset();
        if (cancel) {
            return;
        }

        if (workingBag.getBagIt() == null) {
            LOG.info("Missing bagit.txt file ");
            fireMissingBagIt();
        }


        if (workingBag.getBagInfo() == null) {
            LOG.info("Missing bag-info.txt file ");
            fireMissingBagInfo();
        }

        // initialize digests
        List<String> manifestList = new ArrayList<String>();
        for (String tFile : workingBag.listTagFiles()) {
            if (p.matcher(tFile).matches()) {
                manifestList.add(tFile);
            }
        }

        if (cancel) {
            return;
        }

        //TODO: This may be inefficient, correction this is ineffecient
        MessageDigest[] mdArray = new MessageDigest[manifestList.size()];
        // path,digest[]
        Map<String, String[]> digestMap = new HashMap<String, String[]>();
        for (int i = 0; i < manifestList.size(); i++) {
            DigestEnum de = DigestEnum.valueOfManifest(manifestList.get(i));
            mdArray[i] = de.createDigest();
            loadDigests(digestMap, i, manifestList.get(i), manifestList.size());
        }

        validateFiles(digestMap, mdArray);
    }

    private void validateFiles(Map<String, String[]> digestMap, MessageDigest[] mdArray) {
        resetDigests(mdArray);

        int fileCount = 0;
        for (Map.Entry<String, String[]> entry : digestMap.entrySet()) {
            String file = entry.getKey();
            fileCount++;
            NDC.push("F:" + fileCount);
            LOG.trace("Starting audit of: " + file);
            String[] digests = entry.getValue();
            InputStream is;

            try {
                is = getBag().openDataInputStream(file);
            } catch (IllegalArgumentException e) {
                LOG.info("Cannot open file for validateion: "
                        + file + " in " + getBag().getIdentifier());
                fireMissingFile(file);
                continue;
            }

            InputStream compiledStream = is;
            //TODO: use threaded digest processor
            for (MessageDigest md : mdArray) {
                compiledStream = new DigestInputStream(compiledStream, md);
            }

            if (!readFully(compiledStream, file)) {
                continue;
            }

            logCorruptDigests(file, mdArray, digests);

            NDC.pop();
            LOG.trace("Ending audit of: " + file);
        }

    }

    private boolean readFully(InputStream compiledStream, String file) {
        try {
            while (compiledStream.read(block) != -1) {
            }
        } catch (IOException e) {
            LOG.info("Error reading file for validateion: "
                    + file + " in " + getBag().getIdentifier());
            fireErrorReadingFile(file, e);
            return false;
        } finally {
            try {
                compiledStream.close();
            } catch (IOException e) {
                LOG.error("Error closing " + file, e);
            }
        }
        return true;
    }

    private void logCorruptDigests(String file, MessageDigest[] mdArray, String[] digests) {
        for (int i = 0; i < mdArray.length; i++) {
            String finalDigest = Util.asHexString(mdArray[i].digest());
            if (Util.isEmpty(digests[i])) {
                fireMissingDigest(file);
                LOG.info("Missing digest for " + file + " in "
                        + getBag().getIdentifier());
            } else if (!finalDigest.equals(digests[i])) {
                LOG.info("Corrupt file " + file + " in "
                        + getBag().getIdentifier() + " expected: "
                        + digests[i] + " seen: " + finalDigest);
                fireCorruptFile(file, digests[i], finalDigest);
            }
        }
    }

    private void resetDigests(MessageDigest[] mdArray) {
        for (MessageDigest md : mdArray) {
            md.reset();
        }
    }

    private void loadDigests(Map<String, String[]> digestMap, int idx,
            String manifest, int totalDigests) {

        InputStream is = workingBag.openDataInputStream(manifest);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line;
        try {
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String[] parts = line.split("\\s+", 2);
                if (parts.length != 2) {
                    LOG.info("Bad line: " + line + "from bag: "
                            + getBag().getIdentifier() + " in manifest file: " + manifest);
                    fireBadBagLine(manifest, line);
                    continue;
                }
                String path = parts[1].trim();
                String digest = parts[0].trim();
                if (!digestMap.containsKey(path)) {
                    String[] digList = new String[totalDigests];
                    digList[idx] = digest;
                    digestMap.put(path, digList);

                } else {
                    String[] digList = digestMap.get(path);
                    digList[idx] = digest;
                }
            }
            br.close();
        } catch (IOException e) {
            LOG.error("Error reading file: " + manifest);

        }
    }

    /**
     * Scan the stored bag entry for completeness. Completeness means that all
     * required components (bagit, baginfo, and 1 manifest) exist. In addition
     * it will check to ensure that all data listed in the manifests exist, all
     * manifests match, and no extranious files are found.
     *
     * @return true if the above is met, false otherwise
     */
    public boolean isComplete() {

        if (workingBag.getBagIt() == null) {
            LOG.info("Missing bagit.txt file ");
            return false;
        }

        if (workingBag.getBagInfo() == null) {
            LOG.info("Missing bag-info.txt file ");
            return false;
        }

        // locate manifests
        List<String> files = null;
        try {
            for (String s : workingBag.listTagFiles()) {
                if (p.matcher(s).matches()) {
                    if (files == null) {
                        files = loadManifestFiles(s);
                    } else {
                        List<String> mf2 = loadManifestFiles(s);
                        if (mf2.size() != files.size()) {
                            LOG.info("Differing manifests " + s);
                            return false;
                        }
                        for (int i = 0; i < files.size(); i++) {
                            if (!files.get(i).equals(mf2.get(i))) {
                                LOG.info("Differing manifests " + s);
                                return false;
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            LOG.error("Error reading manifest ", e);
            return false;
        }

        // no manifests
        if (files == null) {
            LOG.error("No manifest found");
            return false;
        }

        // check for all files in manifest
        List<String> dFiles = workingBag.listDataFiles();
        for (String s : files) {
            if (!dFiles.contains(s)) {
                LOG.error("Manifest file not in storage: " + s);
                return false;
            }
            dFiles.remove(s);
        }
        // files on disk, but not in manifest
        if (dFiles.size() > 0) {
            LOG.error("Files on disk, but not in manifest: " + dFiles.size());
            return false;
        }
        return true;
    }

    private List<String> loadManifestFiles(String manifest) throws IOException {
        List<String> fList = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(workingBag.openTagInputStream(manifest)));

        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            String[] parts = line.split("\\s+", 2);
            if (parts.length != 2) {
                br.close();
                throw new IOException("Bad Line: " + line);
            }
            fList.add(parts[1].trim());
        }
        br.close();
        Collections.sort(fList);
        return fList;
    }

    private class MyBagListener implements BagCheckListener {

        private boolean bagInfo = true;
        private boolean bagIt = true;
        private List<String> extraFiles = new ArrayList<String>();
        private List<String> missingFiles = new ArrayList<String>();
        private List<String> corruptFiles = new ArrayList<String>();

        public boolean isValid() {
            return bagInfo && bagIt && extraFiles.isEmpty() && missingFiles.isEmpty()
                    &&  corruptFiles.isEmpty();
        }

        public void badBagLine(BagChecker checker, String file, String line) {
        }

        public void missingFile(BagChecker checker, String file) {
        }

        public void errorReadingFile(BagChecker checker, String file, IOException e) {
        }

        public void missingDigest(BagChecker checker, String file) {
        }

        public void corruptFile(BagChecker checker, String file, String expected, String seen) {
        }

        private void reset() {
            extraFiles.clear();
            missingFiles.clear();
//            manifestDifferences.clear();
            corruptFiles.clear();
            bagInfo = true;
            bagIt = true;
        }

        public void missingBagInfo(BagChecker checker) {
            bagInfo = false;
        }

        public void missingBagIt(BagChecker checker) {
            bagIt = false;
        }

        public void threadEnding(BagChecker checker) {
        }
    }
}
