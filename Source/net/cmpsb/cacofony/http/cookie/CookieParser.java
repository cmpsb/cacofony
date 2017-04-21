package net.cmpsb.cacofony.http.cookie;

import net.cmpsb.cacofony.util.UrlCodec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A parser for cookies.
 *
 * @author Luc Everse
 */
public class CookieParser {
    /**
     * The URL encoder/decoder to use.
     */
    private final UrlCodec urlCodec;

    /**
     * Creates a new cookie parser.
     *
     * @param urlCodec the URL encoder/decoder to use
     */
    public CookieParser(final UrlCodec urlCodec) {
        this.urlCodec = urlCodec;
    }

    /**
     * Parses a string into a cookie.
     *
     * @param plain the source string to parse
     *
     * @return a cookie
     */
    public Cookie parseAttributed(final String plain) {
        final int firstEquals = plain.indexOf('=');
        final int firstSemicolon = plain.indexOf(';');

        // If there is no =, error.
        if (firstEquals == -1) {
            throw new InvalidCookieException();
        }

        // If there are no attributes, split it by equals and return.
        if (firstSemicolon == -1) {
            final String name = this.substr(plain, 0, firstEquals);
            final String value = this.substr(plain, firstEquals + 1);
            return new Cookie(name, value);
        }

        final Cookie cookie;

        // If the equals sign belongs to the crumble, split it there.
        // Otherwise it's an invalid cookie.
        if (firstEquals < firstSemicolon) {
            final String name = this.substr(plain, 0, firstEquals);
            final String value = this.substr(plain, firstEquals + 1, firstSemicolon);
            cookie = new Cookie(name, value);
        } else {
            throw new InvalidCookieException();
        }

        int lastSemicolon = firstSemicolon;
        while (true) {
            // Locate the key between the last semicolon and the next equals sign.
            final int nextEquals = plain.indexOf('=', lastSemicolon + 1);
            final int nextSemicolon = plain.indexOf(';', lastSemicolon + 1);

            if (nextEquals == -1 && nextSemicolon == -1) {
                final String key = this.substr(plain, lastSemicolon + 1).toLowerCase();
                cookie.getAttributes().put(key, null);
                break;
            }

            if (nextSemicolon == -1) {
                // Last attribute with value.
                final String key = this.substr(plain, lastSemicolon + 1, nextEquals).toLowerCase();
                final String value = this.substr(plain, nextEquals + 1);
                cookie.getAttributes().put(key, value);
                break;
            }

            if (nextEquals != -1 && nextEquals < nextSemicolon) {
                // Attribute with value.
                final String key = this.substr(plain, lastSemicolon + 1, nextEquals).toLowerCase();
                final String value = this.substr(plain, nextEquals + 1, nextSemicolon);
                cookie.getAttributes().put(key, value);
            } else {
                final String key = this.substr(plain, lastSemicolon + 1, nextSemicolon);
                cookie.getAttributes().put(key.toLowerCase(), null);
            }

            lastSemicolon = nextSemicolon;
        }

        return cookie;
    }

    /**
     * Parses a semicolon-separated string of cookies into objects.
     *
     * @param plain the string to parse
     *
     * @return the cookies in the string
     */
    public Map<String, List<Cookie>> parseSimple(final String plain) {
        if (plain == null || plain.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, List<Cookie>> cookies = new HashMap<>();

        int lastSemicolon = -1;
        do {
            final int nextEquals = plain.indexOf('=', lastSemicolon + 1);
            final int nextSemicolon = plain.indexOf(';', lastSemicolon + 1);

            if (nextEquals != -1 && nextSemicolon == -1) {
                // Last cookie with value.
                final String name = this.substr(plain, lastSemicolon + 1, nextEquals);
                final String value = this.substr(plain, nextEquals + 1);
                cookies.computeIfAbsent(name, n -> new ArrayList<>()).add(new Cookie(name, value));
                break;
            }

            if (nextEquals != -1 && nextEquals < nextSemicolon) {
                // Cookie with value.
                final String name = this.substr(plain, lastSemicolon + 1, nextEquals);
                final String value = this.substr(plain, nextEquals + 1, nextSemicolon);
                cookies.computeIfAbsent(name, n -> new ArrayList<>()).add(new Cookie(name, value));
            }

            lastSemicolon = nextSemicolon;
        } while (lastSemicolon != -1);

        return cookies;
    }

    /**
     * Takes a substring, trims the whitespace and decodes it from URL encoding.
     *
     * @param string the string to substring
     * @param start  the start position
     * @param end    the end position
     *
     * @return a trimmed and decoded substring
     */
    private String substr(final String string, final int start, final int end) {
        return this.urlCodec.decodeUriComponent(string.substring(start, end).trim());
    }

    /**
     * Takes a substring, trims the whitespace and decodes it from URL encoding.
     *
     * @param string the string to substring
     * @param start  the start position
     *
     * @return a trimmed and decoded substring
     */
    private String substr(final String string, final int start) {
        return this.urlCodec.decodeUriComponent(string.substring(start).trim()).toLowerCase();
    }
}
