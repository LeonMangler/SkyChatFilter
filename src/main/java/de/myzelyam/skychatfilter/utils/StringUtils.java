package de.myzelyam.skychatfilter.utils;

import java.util.Collection;
import java.util.Locale;

public abstract class StringUtils {

    public static boolean startsWithIgnoreCase(String s1, String s2) {
        return s1.toUpperCase(Locale.ENGLISH).startsWith(s2.toUpperCase(Locale.ENGLISH));
    }

    public static boolean containsIgnoreCase(Collection<String> col, String str) {
        for (String str2 : col)
            if (str2.equalsIgnoreCase(str))
                return true;
        return false;
    }

    /*
     * License for this implementation from https://github.com/tdebatty/java-string-similarity:
     *
     * The MIT License
     *
     * Copyright 2015 Thibault Debatty.
     *
     * Permission is hereby granted, free of charge, to any person obtaining a copy
     * of this software and associated documentation files (the "Software"), to deal
     * in the Software without restriction, including without limitation the rights
     * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
     * copies of the Software, and to permit persons to whom the Software is
     * furnished to do so, subject to the following conditions:
     *
     * The above copyright notice and this permission notice shall be included in
     * all copies or substantial portions of the Software.
     *
     * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
     * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
     * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
     * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
     * THE SOFTWARE.
     */
    public static double similarity(final String s1, final String s2) {
        return 1.0 - distance(s1, s2);
    }

    private static double distance(final String s1, final String s2) {
        if (s1 == null) {
            throw new NullPointerException("s1 must not be null");
        }
        if (s2 == null) {
            throw new NullPointerException("s2 must not be null");
        }
        if (s1.equals(s2)) {
            return 0;
        }
        int m_len = Math.max(s1.length(), s2.length());
        if (m_len == 0) {
            return 0;
        }
        return levenshteinDistance(s1, s2) / m_len;
    }

    private static double levenshteinDistance(final String s1, final String s2) {
        if (s1.length() == 0) {
            return s2.length();
        }
        if (s2.length() == 0) {
            return s1.length();
        }
        int[] v0 = new int[s2.length() + 1];
        int[] v1 = new int[s2.length() + 1];
        int[] vtemp;
        for (int i = 0; i < v0.length; i++) {
            v0[i] = i;
        }
        for (int i = 0; i < s1.length(); i++) {
            v1[0] = i + 1;
            int minv1 = v1[0];
            for (int j = 0; j < s2.length(); j++) {
                int cost = 1;
                if (s1.charAt(i) == s2.charAt(j)) {
                    cost = 0;
                }
                v1[j + 1] = Math.min(
                        v1[j] + 1,
                        Math.min(
                                v0[j + 1] + 1,
                                v0[j] + cost));

                minv1 = Math.min(minv1, v1[j + 1]);
            }
            if (minv1 >= Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            vtemp = v0;
            v0 = v1;
            v1 = vtemp;

        }
        return v0[s2.length()];
    }
}
