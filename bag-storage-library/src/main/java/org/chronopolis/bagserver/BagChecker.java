/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.bagserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author toaster
 */
public final class BagChecker {

    private BagEntry workingBag;
    private static final Logger LOG = Logger.getLogger(BagChecker.class);
    private Pattern p = Pattern.compile("^manifest-[a-z0-9]+\\.txt$");
    private List<BagCheckListener> listenerList = new ArrayList<BagCheckListener>();
    private MyBagListener l;
    private boolean cancel = false;

    public BagChecker(BagEntry workingBag) {
        l = new MyBagListener();
        listenerList.add(l);
        this.workingBag = workingBag;
    }

    public void addListener(BagCheckListener listener) {
        listenerList.add(listener);
    }

    public void removeLIstener(BagCheckListener listener) {
        listenerList.remove(listener);
    }

    public List<String> getCorruptFiles() {
        return l.corruptFiles;
    }

    public List<String> getExtraFiles() {
        return l.extraFiles;
    }

    public List<String> getManifestDifferences() {
        return l.manifestDifferences;
    }

    public List<String> getMissingFiles() {
        return l.missingFiles;
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
        
        MessageDigest[] mdArray = new MessageDigest[manifestList.size()];
        // path,digest[]
        Map<String,String[]> digestMap = new HashMap<String, String[]>();
        for (int i = 0; i<manifestList.size(); i++)
        {
            DigestEnum de = DigestEnum.valueOfManifest(manifestList.get(i));
            mdArray[i] = de.createDigest();
            loadDigests(digestMap, i, manifestList.get(i));
        }
    }

    private void validateFiles(Map<String,String[]> digestMap, MessageDigest[] mdArray)
    {
        
    }

    private void loadDigests(Map<String,String[]> digestMap, int idx, String file)
    {
        
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

    private Map<String, String> loadManifestDiests(String manifest) throws IOException {
        Map<String, String> fList = new HashMap<String, String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(workingBag.openTagInputStream(manifest)));

        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            String[] parts = line.split("\\s+", 2);
            if (parts.length != 2) {
                br.close();
                throw new IOException("Bad Line: " + line);
            }
            fList.put(parts[1].trim(), parts[0].trim());
        }
        br.close();

        return fList;
    }

    private class MyBagListener implements BagCheckListener {

        private boolean bagInfo = true;
        private boolean bagIt = true;
        private List<String> extraFiles = new ArrayList<String>();
        private List<String> missingFiles = new ArrayList<String>();
        private List<String> manifestDifferences = new ArrayList<String>();
        private List<String> corruptFiles = new ArrayList<String>();

        public boolean isValid() {
            return bagInfo && bagIt && extraFiles.isEmpty() && missingFiles.isEmpty()
                    && manifestDifferences.isEmpty() && corruptFiles.isEmpty();
        }

        private void reset() {
            extraFiles.clear();
            missingFiles.clear();
            manifestDifferences.clear();
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

        public void threadEnding(BagChecker checker)
        {
            
        }
    }
}
