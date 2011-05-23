/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.bagserver;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * enum mapping from bagit digest names to java digest names
 * TODO: finish mapping, 384/512, etc
 * @author toaster
 */
public enum DigestEnum {

    MD5("md5"), SHA256("sha-256");
    private static Pattern p = Pattern.compile("^manifest-([a-z0-9]+)\\.txt$");
    private String javaDigest;

    private DigestEnum(String javaDigest) {
        this.javaDigest = javaDigest;
    }

    public static DigestEnum valueOfManifest(String digestname) {
        Matcher m = p.matcher(digestname);
        if (!m.matches()) {
            throw new IllegalArgumentException("bad manifest name");
        }
        String digName = m.group(1);
        if ("md5".equals(digName)) {
            return MD5;
        } else if ("sha256".equals(digName)) {
            return SHA256;
        } else {
            throw new IllegalArgumentException("Unsupported digest name");
        }
    }

    public MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance(javaDigest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Invalid digest name: " + javaDigest, e);
        }
    }
}
