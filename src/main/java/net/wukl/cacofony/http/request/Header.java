package net.wukl.cacofony.http.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A request or response header.
 *
 * The header has a non-{@code null} name and zero or more values. Values also can not be
 * {@code null}.
 *
 * When using HTTP/2, it is wise to mark headers that can contain security-sensitive information
 * as sensitive (using
 */
public class Header {
    /**
     * The name of the header.
     */
    private final String key;

    /**
     * The values of the header.
     */
    private final List<String> values;

    /**
     * Whether the header contains sensitive information and should not be compressed.
     */
    private final boolean sensitive;

    /**
     * Creates a new header.
     *
     * @param key the header key
     * @param values the header values
     */
    public Header(final String key, final List<String> values) {
        this(key, values, false);
    }

    /**
     * Creates a new header.
     *
     * @param key the header key
     * @param value the header value
     */
    public Header(final String key, final String value) {
        this(key, value, false);
    }

    /**
     * Creates a new header.
     *
     * The header contains no values.
     *
     * @param key the header key
     */
    public Header(final String key) {
        this(key, false);
    }

    /**
     * Creates a new header.
     *
     * @param key the header key
     * @param values the header values
     * @param sensitive if {@code true}, the header is treated as a security-sensitive part
     *                  of the response
     */
    public Header(final String key, final List<String> values, final boolean sensitive) {
        this.key = key;
        this.values = new ArrayList<>(values);
        this.sensitive = sensitive;
    }

    /**
     * Creates a new header.
     *
     * @param key the header key
     * @param value the header value
     * @param sensitive if {@code true}, the header is treated as a security-sensitive part
     *                  of the response
     */
    public Header(final String key, final String value, final boolean sensitive) {
        this(key, Collections.singletonList(value), sensitive);
    }

    /**
     * Creates a new header.
     *
     * The header contains no values.
     *
     * @param key the header key
     * @param sensitive if {@code true}, the header is treated as a security-sensitive part
     *                  of the response
     */
    public Header(final String key, final boolean sensitive) {
        this(key, Collections.emptyList(), sensitive);
    }

    /**
     * Returns the header key.
     *
     * @return the key
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Returns the header values.
     *
     * @return the values
     */
    public List<String> getValues() {
        return this.values;
    }

    /**
     * Returns the first value.
     *
     * @return the first value
     *
     * @throws IndexOutOfBoundsException if the header contains no values
     */
    public String getFirstValue() {
        return this.values.get(0);
    }

    /**
     * Returns the last value.
     *
     * @return the last value
     *
     * @throws IndexOutOfBoundsException if the header contains no values
     */
    public String getLastValue() {
        return this.values.get(this.values.size() - 1);
    }

    /**
     * Checks whether the header could contain sensitive information.
     *
     * @return {@code true} if the header could contain sensitive information,
     *         {@code false} otherwise
     */
    public boolean isSensitive() {
        return this.sensitive;
    }

    /**
     * Adds all values from the other header to this header.
     *
     * It is an error to merge the values of a header with a different key or to merge the values
     * of a security-sensitive header into a header without that flag set.
     *
     * @param header the other header
     *
     * @throws IllegalArgumentException if the header keys differ (case-insensitive)
     *          or if the source header is sensitive, but the target header is not
     */
    public void add(final Header header) {
        if (!this.key.equalsIgnoreCase(header.key)) {
            throw new IllegalArgumentException(
                    this.key + " and " + header.key + " cannot be merged automatically"
            );
        }

        if (!this.sensitive && header.sensitive) {
            throw new IllegalArgumentException(
                    "Cannot add sensitive values to a normal header. "
                    + "Merge the other way or re-create this header with the sensitive flag set."
            );
        }

        this.values.addAll(header.values);
    }

    /**
     * Checks whether this object is equal to another.
     *
     * The header keys are compared in a case-insensitive manner. The values are compared as-is.
     *
     * @param obj the object to check for equality
     *
     * @return {@code true} if the objects are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Header)) {
            return false;
        }

        final var other = (Header) obj;

        return this.key.equalsIgnoreCase(other.key) && Objects.equals(this.values, other.values);
    }

    /**
     * Generates the hash code for the object.
     *
     * The key is treated in a case-insensitive manner, by converting it to lowercase before hashing
     * the string. The values are used as-is.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.key.toLowerCase(), this.values);
    }

    /**
     * Transforms the header into a human-readable string.
     *
     * Note that the result of this method is not suitable for computer consumption.
     *
     * @return the header as a string
     */
    @Override
    public String toString() {
        return this.key + ": " + this.values.toString();
    }
}
