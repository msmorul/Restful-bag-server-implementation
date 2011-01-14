/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest;

import java.text.DecimalFormat;

/**
 *
 * @author toaster
 */
public class Util {

    private static final String[] units = {" B", " KB", " MB", " GB", " TB", " PB"};

    private Util() {
    }

    public static String formatSize(long size) {
        DecimalFormat df = new DecimalFormat("###,###.#");

        long div = 1;
        long testval = size;
        for (int i = 0; i < 6; i++) {
            if ((testval >>>= 10) == 0) {
                return df.format((double) size / div) + units[i];
            }
            div <<= 10;
        }
        return size + units[0];
    }
}
