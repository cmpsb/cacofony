package net.cmpsb.cacofony.util;

import net.cmpsb.cacofony.http.exception.BadRequestException;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * An encoder and decoder for URL-encoded strings.
 *
 * @author Luc Everse
 */
public class UrlCodec {
    /**
     * A lookup table that lists whether a UTF-8 byte is allowed in a string or not.
     */
    private final boolean[] isAllowedInCookie;

    /**
     * Creates a new URL encoder/decoder.
     */
    public UrlCodec() {
        final String disallowedInCookie = " \"%(),/:;<=>?@[]\\{}";

        this.isAllowedInCookie = new boolean[256];

        for (int i = 0; i < this.isAllowedInCookie.length; ++i) {
            this.isAllowedInCookie[i] = !(
                i <= 0x1F || disallowedInCookie.indexOf(i) >= 0 ||  i >= 0x7F
            );
        }
    }

    /**
     * Decodes a URI part from the RFC encoding.
     *
     * @param str the string to decode
     *
     * @return a decoded string
     *
     * @throws BadRequestException if a %xx sequence can't be decoded to a valid number
     */
    public String decodeUriComponent(final String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (final UnsupportedEncodingException ex) {
            // Not gonna happen. Hopefully.
            throw new RuntimeException(ex);
        } catch (final IllegalArgumentException ex) {
            throw new BadRequestException(ex);
        }
    }

    /**
     * Encodes a URI part using the RFC encoding.
     *
     * @param str the string to encode
     *
     * @return the encoded string
     */
    public String encodeUriComponent(final String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (final UnsupportedEncodingException ex) {
            // Not gonna happen. Hopefully.
            throw new RuntimeException(ex);
        }
    }

    /**
     * Percent encodes a string to be suitable in a cookie.
     *
     * @param component the string to encode
     *
     * @return the encoded string
     */
    public String encodeCookieComponent(final String component) {
        final StringBuilder builder = new StringBuilder();

        final byte[] asUtf8 = component.getBytes(StandardCharsets.UTF_8);

        for (final int b : asUtf8) {
            final int octet = b & 0xFF;
            if (!this.isAllowedInCookie[octet]) {
                builder.append(String.format("%%%02X", octet));
            } else {
                builder.append((char) octet);
            }
        }

        return builder.toString();
    }

    /**
     * Decodes a string using percent encoding.
     *
     * @param component the string to decode
     *
     * @return the decoded string
     *
     * @throws BadRequestException if a %xx sequence can't be decoded to a valid number
     */
    public String decodeCookieComponent(final String component) {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        final byte[] asUtf8 = component.getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i < asUtf8.length; ++i) {
            // Handle an escaped sequence.
            if (asUtf8[i] == '%') {
                // Extract the two hex digits.
                final String hex = new String(asUtf8, i, 2, StandardCharsets.UTF_8);
                int cp;
                try {
                    // Decode them as a hexadecimal number and append it to the buffer.
                    cp = Integer.parseInt(hex, 16);
                    buffer.write(cp);
                    i += 2;
                } catch (final NumberFormatException ex) {
                    // Error if the format is not okay.
                    throw new BadRequestException("Illegal hex tuple: " + hex);
                }
            } else {
                buffer.write(asUtf8[i]);
            }
        }

        try {
            return buffer.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Not going to happen.
            throw new RuntimeException(e);
        }
    }
}
