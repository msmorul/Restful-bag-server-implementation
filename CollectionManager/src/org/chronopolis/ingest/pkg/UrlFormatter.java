/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.pkg;

import edu.umiacs.ace.json.Strings;
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
