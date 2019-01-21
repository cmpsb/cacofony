package net.wukl.cacofony.http.request;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A parser for query strings.
 *
 * @author Luc Everse
 */
public class QueryStringParser {
    /**
     * Parses a query string.
     *
     * @param str the source string to read
     *
     * @return a map containing the query string parameters
     */
    public Map<String, String> parse(final String str) {
        if (str.isEmpty() || str.equals("?")) {
            return Collections.emptyMap();
        }

        final Map<String, String> map = new HashMap<>();

        int previousSeparator = Math.min(str.indexOf('?'), 0);
        while (true) {
            final int nextSeparator = str.indexOf('&', previousSeparator + 1);
            final int nextEquals = str.indexOf('=', previousSeparator + 1);

            if (nextSeparator == -1) {
                // The string has ended. Parse the last value.
                if (nextEquals == -1) {
                    final String key = this.decodeSubstring(str, previousSeparator + 1);
                    map.put(key, "");
                } else {
                    final String key = this.decodeSubstring(str, previousSeparator + 1, nextEquals);
                    final String value = this.decodeSubstring(str, nextEquals + 1);
                    map.put(key, value);
                }
                break;
            }

            if (nextEquals == -1 || nextEquals > nextSeparator) {
                final String key = this.decodeSubstring(str, previousSeparator + 1, nextSeparator);
                map.put(key, "");
            } else {
                final String key = this.decodeSubstring(str, previousSeparator + 1, nextEquals);
                final String value = this.decodeSubstring(str, nextEquals + 1, nextSeparator);
                map.put(key, value);
            }

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
