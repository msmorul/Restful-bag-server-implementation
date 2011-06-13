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
