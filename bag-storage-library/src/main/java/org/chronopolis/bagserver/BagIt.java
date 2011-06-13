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
import java.io.Reader;
import java.io.Writer;

/**
 *
 * @author toaster
 */
public class BagIt {

    public static final String FILE_NAME = "bagit.txt";
    public static final String VERSION = "BagIt-Version";
    public static final String ENCODING = "Tag-File-Character-Encoding";
    public String encoding;
    public String version;

    public BagIt() {
    }

    BagIt(String encoding, String version) {
        setEncoding(encoding);
        setVersion(version);
    }

    public String getEncoding() {
        return encoding;
    }

    public String getVersion() {
        return version;
    }

    final void setEncoding(String encoding) {
        this.encoding = encoding.trim();
    }

    final void setVersion(String version) {
        this.version = version.trim();
    }

    public static BagIt readFile(Reader is) throws IOException {
        BufferedReader br;
        String version, encoding;

        if (!(is instanceof BufferedReader)) {
            br = new BufferedReader(is);
        } else {
            br = (BufferedReader) is;
        }
        String versionLine = br.readLine();
        String encodingLine = br.readLine();

        String[] vl2 = versionLine.split(":");
        if (vl2.length != 2 || !vl2[0].equals(VERSION)) {
            throw new IOException("Bad version line: " + versionLine);
        } else {
            version = vl2[1].trim();
        }

        String[] el2 = encodingLine.split(":");
        if (el2.length != 2 || !el2[0].equals(ENCODING)) {
            throw new IOException("Bad encoding line: " + encodingLine);
        } else {
            encoding = el2[1].trim();
        }

        return new BagIt(encoding, version);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (this.encoding != null ? this.encoding.hashCode() : 0);
        hash = 17 * hash + (this.version != null ? this.version.hashCode() : 0);
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
        final BagIt other = (BagIt) obj;
        if ((this.encoding == null) ? (other.encoding != null) : !this.encoding.equals(other.encoding)) {
            return false;
        }
        if ((this.version == null) ? (other.version != null) : !this.version.equals(other.version)) {
            return false;
        }
        return true;
    }

    public void writeFile(Writer os) throws IOException {
        os.write(VERSION);
        os.write(": ");
        os.write(version);
        os.write("\n");
        os.write(ENCODING);
        os.write(": ");
        os.write(encoding);
        os.write("\n");
    }
}
