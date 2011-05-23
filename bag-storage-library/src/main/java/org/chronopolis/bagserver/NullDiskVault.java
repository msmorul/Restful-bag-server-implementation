/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.bagserver;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple vault that only provides two static bags (cbag, wbag) with one item
 * containing a bs digest.
 * 
 * @author toaster
 */
public class NullDiskVault implements BagVault {

    private static String COMMITTED_BAG = "cbag";
    private static String WORK_BAG = "wbag";
    private boolean workDeleted = false;
    private boolean commitDeleted = false;
    private static final String MANIFEST = "a4df   data/test";

    public BagEntry getBag(String identifier) {
        if (!workDeleted && WORK_BAG.equals(identifier)) {
            return new NullBagEntry(true);
        }
        if (!commitDeleted && COMMITTED_BAG.equals(identifier)) {
            return new NullBagEntry(false);
        }
        return null;
    }

    public List<BagEntry> getBags() {
        List<BagEntry> al = new ArrayList<BagEntry>();

        if (!workDeleted) {
            al.add(new NullBagEntry(true));
        }
        if (!commitDeleted) {
            al.add(new NullBagEntry(false));
        }
        return al;
    }

    public BagEntry createNewBag(String newIdentifier) {
        throw new IllegalStateException("Creating bags not supported");
    }

    public boolean bagExists(String identifier) {
        return !workDeleted && WORK_BAG.equals(identifier) || !commitDeleted && COMMITTED_BAG.equals(identifier);
    }

    public static class NullBagEntry implements BagEntry {

        boolean isWork;

        NullBagEntry(boolean isWork) {
            this.isWork = isWork;
        }

        public List<String> listDataFiles() {
            List<String> l = new ArrayList<String>();
            l.add("data/test");
            return l;
        }

        public List<String> listTagFiles() {
            List<String> l = new ArrayList<String>();
            l.add("bag-info.txt");
            l.add("bagit.txt");
            l.add("manifest-md5.txt");
            return l;
        }

        public String getIdentifier() {
           if (isWork)
               return WORK_BAG;
           return COMMITTED_BAG;
        }

        public State getBagState() {
            if (isWork) {
                return State.OPEN;
            }
            return State.COMMITTED;
        }

        public boolean isComplete() {
            return true;
        }

        public boolean commit() {
            return true;
        }

        public boolean delete() {
            return true;
        }

        public boolean setBagItInformation(BagIt bagIt) {
            return true;
        }

        public boolean setBagInfo(BagInfo baginfo) {
            return true;
        }

        public BagInfo getBagInfo() {
            Map<String, String> namemap = new HashMap<String, String>();
            if (isWork) {
                namemap.put("External-Identifier", WORK_BAG);
            } else {
                namemap.put("External-Identifier", COMMITTED_BAG);
            }

            return new BagInfo(namemap);
        }

        public BagIt getBagIt() {
            return new BagIt("UTF-8", "0.96");
        }

        public InputStream openTagInputStream(String tagItem) throws IllegalArgumentException {
            if (tagItem.equals("manifest-md5.txt")) {
                return new ByteArrayInputStream(MANIFEST.getBytes());
            }
            throw new IllegalArgumentException("BAd identifier");
        }

        public OutputStream openTagOutputStream(String tagItem) throws IllegalArgumentException {
            throw new IllegalArgumentException("Cannot write to null driver");
        }

        public OutputStream openDataOutputStream(String fileIdentifier) throws IllegalArgumentException {
            throw new IllegalArgumentException("No writing allowed");
        }

        public InputStream openDataInputStream(String fileIdentifier) throws IllegalArgumentException {
            if (fileIdentifier.equals("data/test")) {
                return new ByteArrayInputStream(MANIFEST.getBytes());
            }
            throw new IllegalArgumentException("BAd identifier");
        }
    }
}
