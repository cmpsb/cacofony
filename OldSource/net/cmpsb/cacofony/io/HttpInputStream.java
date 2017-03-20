package net.cmpsb.cacofony.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * A mixed text and binary input stream for reading HTTP messages.
 *
 * @author Luc Everse
 */
public class HttpInputStream extends InputStream {
    /**
     * The default size of the internal buffer.
     */
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * The source stream to read from.
     */
    private final InputStream source;

    /**
     * The buffer to temporarily store bytes in.
     */
    private final byte[] buffer;

    /**
     * A pointer into the buffer where the next read will start.
     */
    private int pointer = 0;

    /**
     * The number of available bytes from the pointer to the end of the buffer.
     */
    private int available = 0;

    /**
     * Create a new HTTP input stream.
     *
     * @param source     the source stream
     * @param bufferSize the size of the internal buffer
     */
    public HttpInputStream(final InputStream source, final int bufferSize) {
        if (bufferSize < 0) {
            throw new IllegalArgumentException("The buffer size must be at least 0.");
        }

        if (source == null) {
            throw new IllegalArgumentException("The source stream is null.");
        }

        this.source = source;

        this.buffer = new byte[bufferSize];
    }

    /**
     * Create a new HTTP input stream.
     *
     * @param source the source stream
     */
    public HttpInputStream(final InputStream source) {
        this(source, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Ensure that the source stream is open.
     *
     * @throws IOException if the source stream is closed
     */
    private void ensureOpen() throws IOException {
        if (this.source == null) {
            throw new IOException("The stream has been closed.");
        }
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * A subclass must provide an implementation of this method.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     * stream is reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        this.ensureOpen();

        // If there's still bytes in the buffer, return the next one.
        if (this.pointer < this.available) {
            final int value = this.buffer[this.pointer];
            ++this.pointer;
            return value;
        }

        // Otherwise, try to load from the source stream.
        this.load();

        // Return EOF if the source stream says so.
        if (this.available == -1) {
            return -1;
        }

        // The buffer's fresh, get the next byte.
        this.pointer = 1;

        return this.buffer[0];
    }

    /**
     * Read a full line from the stream.
     * The encoding is assumed to be ISO-8859-1, but only the US-ASCII interpretation can be
     * guaranteed.
     * A line is delimited by a carriage return and a line feed, also denoted as "CRLF" or "\r\n".
     *
     * @return a single read line
     *
     * @throws IOException if an I/O error occurs while reading
     */
    public String readLine() throws IOException {
        final StringBuilder lineBuilder = new StringBuilder();

        while (true) {
            // If this is EOL, finish the string.
            if (this.available == -1) {
                return lineBuilder.toString();
            }

            // If we exhausted all characters, reload the buffer once more.
            if (this.pointer >= this.available) {
                this.load();
            }

            // Get the first character.
            final int character = this.buffer[this.pointer];
            ++this.pointer;

            // If this character is a CR, check whether the next is an LF.
            if (character == '\r') {
                // First make sure there's still characters left.
                if (this.pointer >= this.available) {
                    this.load();
                }

                // If the source buffer has been exhausted, return the string.
                if (this.available == -1) {
                    lineBuilder.append((char) character);
                    return lineBuilder.toString();
                }

                final int possibleLf = this.buffer[this.pointer];

                // If it is, terminate the string.
                if (possibleLf == '\n') {
                    return lineBuilder.toString();
                }

                // It isn't, this character may be another CR, so start over from this point.
                // First push the just-read CR.
                lineBuilder.append('\r');
                continue;
            }

            // The character is normal, put it on the string.
            lineBuilder.append((char) character);
        }
    }

    /**
     * Refresh the buffer with new data from the source stream.
     * This resets the standing buffer and its pointer, so any information left will be
     * destroyed.
     *
     * @throws IOException if an I/O error occurs while reading
     */
    private void load() throws IOException {
        this.available = this.source.read(this.buffer);
        this.pointer = 0;
    }
}
