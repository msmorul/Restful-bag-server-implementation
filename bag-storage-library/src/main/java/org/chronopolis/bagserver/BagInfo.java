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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author toaster
 */
public class BagInfo {

    public static final String FILE_NAME = "bag-info.txt";
    private Map<String, String> bagAttributes;

    public BagInfo() {
    }

    BagInfo(Map<String, String> bagAttributes) {
        this.bagAttributes = bagAttributes;
    }

    public Map<String, String> getBagAttributes() {
        return new HashMap(bagAttributes);
    }

    public String setBagAttribute(String attribute, String value) {
        return bagAttributes.put(attribute.trim(), value.trim());
    }

    public String removeAttribute(String attribute) {
        return bagAttributes.remove(attribute);
    }

    public String getAttribute(String attribute) {
        return bagAttributes.get(attribute);
    }

    /**
     * Initialize hash map with bag attributes:
     *
     * @param r
     * @return new baginfo
     * @throws IOException
     */
    public static BagInfo readInfo(Reader r) throws IOException {
        BufferedReader br;
        Map<String, String> pairs = new HashMap<String, String>();

        if (!(r instanceof BufferedReader)) {
            br = new BufferedReader(r);
        } else {
            br = (BufferedReader) r;
        }

//        StringBuilder currentAttribute = new StringBuilder();
        String lastAttribute = null;
        String valueLine = null;
        String currLine;

        while ((currLine = br.readLine()) != null) {
            if (currLine.matches("^\\s+")) {
                //continuation
                valueLine = valueLine + " " + currLine.trim();
                if (lastAttribute == null) {
                    throw new IOException("unexpected line continuation: " + currLine);
                }
            } else {
                if (lastAttribute != null) {
                    pairs.put(lastAttribute, valueLine);
                }
                String[] attrValue = currLine.split(":", 2);
                lastAttribute = attrValue[0].trim();
                valueLine = attrValue[1].trim();
            }
        }
        // final line
        if (lastAttribute != null) {
            pairs.put(lastAttribute, valueLine);
        }

        return new BagInfo(pairs);
    }

    public void writeInfo(Writer w) throws IOException {
        for (Entry<String, String> entry : bagAttributes.entrySet()) {
            w.write(entry.getKey());
            w.write(": ");
            w.write(entry.getValue());
            w.write("\n");
        }
    }
}
