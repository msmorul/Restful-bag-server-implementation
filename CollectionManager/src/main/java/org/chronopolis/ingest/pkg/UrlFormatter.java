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

import java.util.regex.Matcher;

/**
 *
 * @author toaster
 */
public class UrlFormatter {

    private String pattern;
    private String replacementPkg = "";

    public UrlFormatter(ChronPackage pkg, String pattern) {
        this.replacementPkg = Matcher.quoteReplacement(pkg.getName());
        if (pattern == null) {
            throw new IllegalArgumentException("Null URL Pattern");
        }
        this.pattern = pattern;
    }

    public UrlFormatter(String pattern) {

        if (pattern == null) {
            throw new IllegalArgumentException("Null URL Pattern");
        }
        this.pattern = pattern;
    }

    public void setPackage(ChronPackage pkg) {
        if (pkg != null) {
            this.replacementPkg = Matcher.quoteReplacement(pkg.getName());
        } else {
            replacementPkg = "";
        }
    }

    /**
     * Create a formatted URL according to the stored pattern.
     * 
     * @param item path relative to directory root in a package
     * @return string url encoded and generated according to the stored pattern
     */
    public String format(String item) {

        String rawpath = Matcher.quoteReplacement(item);
        String datapath = "data/" + rawpath;

        String url = pattern.replaceAll("\\{b\\}",replacementPkg)
                .replaceAll("\\{d\\}", datapath)
                .replaceAll("\\{r\\}", rawpath);
        return URLUTF8Encoder.encode(url);
    }
}
