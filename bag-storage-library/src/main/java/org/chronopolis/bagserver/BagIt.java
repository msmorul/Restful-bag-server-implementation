/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
