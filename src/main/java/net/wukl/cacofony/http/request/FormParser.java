package net.wukl.cacofony.http.request;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A parser for form data.
 *
 * @author Luc Everse
 */
public class FormParser {
    /**
     * Parses a string containing form-encoded data.
     *
     * @param str the string
     *
     * @return a mapping containing the form data
     */
    public Map<String, List<String>> parse(final String str) {
        if (str.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, List<String>> map = new HashMap<>();

        int previousSeparator = -1;
        while (true) {
            final int nextSeparator = str.indexOf('&', previousSeparator + 1);
            final int nextEquals = str.indexOf('=', previousSeparator + 1);

            final String key;
            final String value;

            if (nextSeparator == -1) {
                // The string has ended. Parse the last value.

                if (nextEquals == -1) {
                    key = this.decodeSubstring(str, previousSeparator + 1);
                    value = "";
                } else {
                    key = this.decodeSubstring(str, previousSeparator + 1, nextEquals);
                    value = this.decodeSubstring(str, nextEquals + 1);
                }

                map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);

                break;
            }

            if (nextEquals == -1 || nextEquals > nextSeparator) {
                key = this.decodeSubstring(str, previousSeparator + 1, nextSeparator);
                value = "";
            } else {
                key = this.decodeSubstring(str, previousSeparator + 1, nextEquals);
                value = this.decodeSubstring(str, nextEquals + 1, nextSeparator);
            }

            map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);

            previousSeparator = nextSeparator;
        }

        return map;
    }

    /**
     * URL decodes a substring.
     *
     * @param str   the string to substring
     * @param start the start of the substring, inclusive
     * @param end   the end of the substring, exclusive
     *
     * @return a URL-decoded substring of {@code str}
     */
    private String decodeSubstring(final String str, final int start, final int end) {
        return this.decodeUriComponent(str.substring(start, end));
    }

    /**
     * URL decodes a substring.
     *
     * @param str   the string to substring
     * @param start the start of the substring, inclusive
     *
     * @return a URL-decoded substring of {@code str}
     */
    private String decodeSubstring(final String str, final int start) {
        return this.decodeUriComponent(str.substring(start));
    }

    /**
     * Decodes a URI part from the RFC encoding.
     *
     * @param str the string to decode
     *
     * @return a decoded string
     */
    private String decodeUriComponent(final String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (final UnsupportedEncodingException ex) {
            // Not gonna happen.
            throw new RuntimeException(ex);
        }
    }
}
