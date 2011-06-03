/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.bagserver;

import java.math.BigInteger;

/**
 *
 * @author toaster
 */
public class Util {

    public static String repeat(char value, int times) {
        if (times <= 0) {
            return "";
        }
        char[] c = new char[times];
        for (int i = 0; i < times; i++) {
            c[i] = value;
        }
        return new String(c);
    }

    public static String asHexString(byte[] value) {
        String str = new BigInteger(1, value).toString(16);
        if (str.length() < value.length * 2) {
            str = repeat('0', value.length * 2 - str.length()) + str;
        }
        return str;
    }

    public static boolean isEmpty(Object value) {
        if (value != null) {
            String sv = value.toString();
            if (sv != null) {
                return sv.trim().length() == 0;
            }
        }
        return true;
    }
}
