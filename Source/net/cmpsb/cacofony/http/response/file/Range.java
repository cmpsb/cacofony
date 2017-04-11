package net.cmpsb.cacofony.http.response.file;

import java.util.Objects;

/**
 * A representation of an HTTP/1.1 range in a ranged request.
 *
 * @author Luc Everse
 */
public final class Range {
    /**
     * The start of the range.
     */
    private final long start;

    /**
     * The end of the range.
     */
    private final long end;

    /**
     * Creates a new range.
     *
     * @param start the first byte of the range
     * @param end   the last byte of the range
     *
     * @throws IllegalArgumentException if {@code end < start}
     */
    public Range(final long start, final long end) {
        if (end < start) {
            throw new IllegalArgumentException("End byte is before start byte.");
        }

        this.start = start;
        this.end = end;
    }

    /**
     * Returns the position of the first byte of the range.
     *
     * @return the first byte
     */
    public long getStart() {
        return this.start;
    }

    /**
     * Returns the position of the last byte of the range.
     *
     * @return the last byte
     */
    public long getEnd() {
        return this.end;
    }

    /**
     * Returns the number of bytes in the range.
     *
     * @return the number of bytes in the range
     */
    public long getLength() {
        return this.end - this.start + 1;
    }

    /**
     * Serializes the range as a string.
     *
     * @return the range as a string
     */
    @Override
    public String toString() {
        return this.start + "-" + this.end;
    }

    /**
     * Checks whether this object is equal to another.
     *
     * @param obj the other object
     *
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Range)) {
            return false;
        }

        final Range other = (Range) obj;

        return this.start == other.start && this.end == other.end;
    }

    /**
     * Calculates the object's hash code.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.start, this.end);
    }
}
