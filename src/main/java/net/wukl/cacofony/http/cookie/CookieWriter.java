package net.wukl.cacofony.http.cookie;

import net.wukl.cacofony.util.UrlCodec;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A writer for cookies.
 *
 * @author Luc Everse
 */
public class CookieWriter {
    /**
     * The URL encoder/decoder to use.
     */
    private final UrlCodec urlCodec;

    /**
     * Creates a new cookie writer.
     *
     * @param codec the URL encoder/decoder
     */
    public CookieWriter(final UrlCodec codec) {
        this.urlCodec = codec;
    }

    /**
     * Writes a cookie with attributes to a string.
     *
     * @param cookie the cookie to write
     *
     * @return the written cookie
     */
    public String writeAttributed(final Cookie cookie) {
        final StringBuilder builder = new StringBuilder(this.encodeCrumble(cookie));

        for (final String attribute : cookie.getAttributes().keySet()) {
            builder.append("; ");
            builder.append(this.encodePair(attribute, cookie.getAttributes().get(attribute)));
        }

        return builder.toString();
    }

    /**
     * Writes a list of cookies as a simple string.
     * <p>
     * Writing a list of cookies
     * <pre>
     * {@code
     *  Arrays.asList(
     *      new Cookie("one", "value1"),
     *      new Cookie("two", "value2")
     *  );
     * }
     * </pre>
     * will result in the string {@code one=value1; two=value2}.
     *
     * @param cookies the cookies to write
     *
     * @return a string containing the cookie crumbles
     */
    public String writeSimpleBulk(final Collection<Cookie> cookies) {
        return cookies.stream()
                .map(this::encodeCrumble)
                .collect(Collectors.joining("; "));
    }

    /**
     * Encodes a cookie crumble.
     *
     * @param cookie the cookie to encode
     *
     * @return the encoded crumble
     */
    private String encodeCrumble(final Cookie cookie) {
        final String encodedValue = this.urlCodec.encodeCookieComponent(cookie.getValue());
        return this.encodePair(cookie.getName(), encodedValue);
    }

    /**
     * Encodes a key-value pair with an optional value.
     *
     * @param key   the key, to the left of the equals sign
     * @param value the optional value, to the right of the equals sign
     *
     * @return the URL-encoded key-value pair
     */
    private String encodePair(final String key, final String value) {
        if (value == null || value.isEmpty()) {
            return key;
        }

        return key + '=' + value;
    }
}
