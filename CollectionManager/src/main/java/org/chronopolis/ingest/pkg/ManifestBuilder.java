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

    public ChronPackage getPackage() {
        return pkg;
    }



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
