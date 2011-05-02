/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

    private Map<String, String> bagAttributes;

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
                String[] attrValue = currLine.split("/", 2);
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
