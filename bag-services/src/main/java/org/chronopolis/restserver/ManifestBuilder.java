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
package org.chronopolis.restserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.chronopolis.bagserver.BagEntry;

/**
 *
 * @author toaster
 */
public class ManifestBuilder {

    private Pattern ptn = Pattern.compile("^manifest-([a-z0-9]+)\\.txt$");
    private List<String> tagFiles = new ArrayList<String>();
    // {alg:{path:value}}
    private Map<String, Map<String, String>> digestMap = new HashMap<String, Map<String, String>>();
    private Set<String> set = new HashSet<String>();

    public ManifestBuilder() {
    }

    public void parseTagFile(String tagFile, BagEntry bag) throws IOException {
        Matcher m = ptn.matcher(tagFile);
        tagFiles.add(tagFile);
        if (m.matches()) {
            String alg = m.group(1);
            digestMap.put(alg, new HashMap<String, String>());
            readManifest( bag.openTagInputStream(tagFile), alg);
        }
    }

    private void readManifest(InputStream is, String alg) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            String[] parts = line.split("\\s+", 2);
            if (parts.length != 2) {
                br.close();
                throw new IOException("Bad Line: " + line);
            }
            String path = parts[1].trim();
            String digest = parts[0].trim();
            set.add(path);
            digestMap.get(alg).put(path, digest);
        }
        br.close();
    }

    Set<String> getPayloadPaths() {
        return set;
    }

    String getDigest(String path, String alg) {
        if (digestMap.containsKey(alg) && digestMap.get(alg).containsKey(path)) {
            return digestMap.get(alg).get(path);
        }
        return null;
    }

    Set<String> listAlgorithms() {
        return digestMap.keySet();
    }

    List<String> getTagFiles() {
        return tagFiles;
    }
}
