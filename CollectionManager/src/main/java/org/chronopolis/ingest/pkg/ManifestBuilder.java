/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.pkg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author toaster
 */
public class ManifestBuilder {

    private List<ManifestBuildListener> buildListeners = new ArrayList<ManifestBuildListener>();
    private ChronPackage pkg;
    private MessageDigest digest;
    private byte[] tossBlock = new byte[32768];
    private boolean execute = true;
    private long totalSize;

    public ManifestBuilder(ChronPackage pkg, long totalSize) {
        this.pkg = pkg;
        this.totalSize = totalSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void cancel() {
        execute = false;
    }

    public List<ManifestBuildListener> getBuildListeners() {
        return buildListeners;
    }

    public void scanPackage() throws IOException {
        execute = true;
        try {
            digest = MessageDigest.getInstance(pkg.getDigest());
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        notifyBuildStart();
        for (File root : pkg.getRootList()) {
            if (!execute) {
                return;
            }
            notifyRootStart(root);
            int subStrLen = root.getPath().length() - root.getName().length();
            processDir(root, subStrLen);
            notifyRootEnd(root);
        }
        notifyEndItems();
        notifyBuildEnd();
    }

    public void processDir(File dir, int substr) {
        for (File f : dir.listFiles()) {
            if (!execute) {
                return;
            }
            if (f.isDirectory()) {
                processDir(f, substr);
            } else {
                processFile(f, substr);
            }
        }
    }

    public boolean isExecute() {
        return execute;
    }

    private void processFile(File f, int substr) {
        String name = f.getPath().substring(substr);

        digest.reset();
        try {
            notifyStartItem(name, f.length());
            DigestInputStream dis = new DigestInputStream(new FileInputStream(f), digest);
            int read = 0;
            while ((read = dis.read(tossBlock)) != -1) {
                notifyWriteBytes(tossBlock, 0, read);
            }
            dis.close();
            byte[] dig = digest.digest();
            String strDigest = asHexString(dig);
            notifyEndItem(name, strDigest);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String repeat(char value, int times) {
        if (times == 0) {
            return "";
        }
        char[] c = new char[times];
        for (int i = 0; i < times; i++) {
            c[i] = value;
        }
        return new String(c);
    }

    public static String asHexString(byte[] value) {
        String str = new BigInteger(1, value).toString(16);
        if (str.length() < value.length * 2) {
            str = repeat('0', value.length * 2 - str.length()) + str;
        }
        return str;
    }

    private void notifyBuildStart() {
        for (ManifestBuildListener l : buildListeners) {
            l.startBuild(this);
        }
    }

    private void notifyEndItems() {
        for (ManifestBuildListener l : buildListeners) {
            l.endItems(this);
        }
    }

    private void notifyBuildEnd() {
        for (ManifestBuildListener l : buildListeners) {
            l.endBuild(this);
        }
    }

    private void notifyEndItem(String file, String digest) {
        for (ManifestBuildListener l : buildListeners) {
            l.endItem(this, file, digest);
        }
    }

    private void notifyStartItem(String item, long size) {
        for (ManifestBuildListener l : buildListeners) {
            l.startItem(this, size, item);
        }
    }

    private void notifyRootStart(File root) {
        for (ManifestBuildListener l : buildListeners) {
            l.startRoot(this, root);
        }
    }

    private void notifyRootEnd(File f) {
        for (ManifestBuildListener l : buildListeners) {
            l.endRoot(this, f);
        }
    }

    private void notifyWriteBytes(byte[] block, int offset, int length) {
        for (ManifestBuildListener l : buildListeners) {
            l.writeBytes(this, block, offset, length);
        }
    }
}
