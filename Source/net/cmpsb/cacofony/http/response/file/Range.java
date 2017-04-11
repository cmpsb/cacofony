package net.cmpsb.cacofony.http.response.file;

/**
 * A representation of an HTTP/1.1 range in a ranged request.
 *
 * @author Luc Everse
 */
public class Range {
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
}
