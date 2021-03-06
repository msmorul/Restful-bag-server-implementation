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
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

/**
 *
 * @author toaster
 */
public class BagWriter {

    public static String INFO_SOURCE_ORGANIZATION = "Source-Organization";
    public static String INFO_ORGANIZATION_ADDRESS = "Organization-Address";
    public static String INFO_CONTACT_NAME = "Contact-Name";
    public static String INFO_CONTACT_PHONE = "Contact-Phone";
    public static String INFO_CONTACT_EMAIL = "Contact-Email";
    public static String INFO_EXTERNAL_DESCRIPTION = "External-Description";
    public static String INFO_BAGGING_DATE = "Bagging-Date";
    public static String INFO_EXTERNAL_IDENTIFIER = "External-Identifier";
    public static String INFO_BAG_SIZE = "Bag-Size";
    public static String INFO_PAYLOAD_OXUM = "Payload-Oxum";
    public static String INFO_BAG_GROUP_IDENTIFIER = "Bag-Group-Identifier";
    public static String INFO_BAG_COUNT = "Bag-Count";
    public static String INFO_INTERNAL_SENDER_IDENTIFIER = "Internal-Sender-Identifier";
    public static String INFO_INTERNAL_SENDER_DESCRIPTION = "Internal-Sender-Description";
    public static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private TarArchiveOutputStream os;
    private boolean isEntryOpen = false;
    private List<String> fetchList = new ArrayList<String>(1000);
    private List<String> digestList = new ArrayList<String>(1000);
    private Map<String, String> metadataMap = new HashMap();
    private ChronPackage pkg;

    public BagWriter(OutputStream os, ChronPackage pkg) {
        this.os = new TarArchiveOutputStream(os);
        this.os.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        this.pkg = pkg;
    }

    /**
     * Add a fetch entry to the bag. This will clean url's to ensure they
     * are properly escaped.
     * 
     * @param url
     * @param size
     * @param relpath
     */
    public void addFetchEntry(String url, long size, String relpath) {

        fetchList.add(url + "  " + size + "  " + relpath);
    }

    public void addDigestEntry(String relpath, String digest) {
        digestList.add(digest + "  " + relpath);
    }

    public void addMetadata(String key, String value) {
        metadataMap.put(key, value);
    }

    /**
     * Finish tar file w/o closing underlying output stream
     *
     */
    public void finish() throws IOException {
        writeInfo();
        writeVersion();
        writeList("fetch.txt", fetchList);
        writeList("manifest-" + pkg.getBagFormattedDigest() + ".txt", digestList);

        
        os.close();
    }

    private String getTarPath(String pkgPath) {
        if (pkg.getName() == null || pkg.getName().isEmpty()) {
            return "bag/" + pkgPath;
            
        } else {
            return pkg.getName() + "/" + pkgPath;
            
        }
    }

    public void openEntry(String relPath, long size) throws IOException {
        if (isEntryOpen) {
            throw new IllegalStateException("Tar entry already open");
        }

        isEntryOpen = true;
        TarArchiveEntry entry = new TarArchiveEntry(getTarPath(relPath));
        if (size > TarArchiveEntry.MAXSIZE) {
            throw new IllegalArgumentException("File size too large " + size);
        }
        entry.setSize(size);
        os.putArchiveEntry(entry);

    }

    public void writeContent(byte[] block, int offset, int length) throws IOException {
        if (!isEntryOpen) {
            throw new IllegalStateException("Tar entry not open");
        }
        os.write(block, offset, length);
    }

    public void closeEntry() throws IOException {
        isEntryOpen = false;
        os.flush();
        os.closeArchiveEntry();
    }

    private void writeInfo() {
        if (!metadataMap.isEmpty()) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);

                for (Map.Entry<String, String> entry : metadataMap.entrySet()) {
                    pw.println(entry.getKey() + ": " + entry.getValue());
                }

                byte[] block = sw.toString().getBytes("UTF-8");
                openEntry("bag-info.txt", block.length);
                writeContent(block, 0, block.length);
                closeEntry();
            } catch (IOException ex) {
                throw new RuntimeException("Could not write header", ex);
            }

        }
    }

    private void writeVersion() {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println("BagIt-Version: 0.96");
            pw.println("Tag-File-Character-Encoding: UTF-8");
            byte[] block = sw.toString().getBytes("UTF-8");
            openEntry("bagit.txt", block.length);
            writeContent(block, 0, block.length);
            closeEntry();
        } catch (IOException ex) {
            throw new RuntimeException("Could not write header", ex);
        }
    }

    private void writeList(String name, List<String> list) throws IOException {
        if (list.size() == 0) {
            return;
        }
        File tmpFile = File.createTempFile("list", ".txt");
        PrintWriter writer = new PrintWriter(new FileWriter(tmpFile));
        for (String s : list) {
            writer.println(s);
        }
        writer.close();

        openEntry(name, tmpFile.length());
        FileInputStream fis = new FileInputStream(tmpFile);
        byte[] block = new byte[32786];
        int read;
        while ((read = fis.read(block)) != -1) {
            writeContent(block, 0, read);
        }
        fis.close();
        closeEntry();

    }
}
