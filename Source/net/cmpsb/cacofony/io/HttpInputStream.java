package net.cmpsb.cacofony.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * A mixed text and binary input stream for reading HTTP messages.
 *
 * @author Luc Everse
 */
public class HttpInputStream extends LineAwareInputStream {
    /**
     * The default size of the internal buffer.
     */
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * The source stream to read from.
     */
    private InputStream source;

    /**
     * The buffer to temporarily store bytes in.
     */
    private byte[] buffer;

    /**
     * A pointer into the buffer where the next read will start.
     */
    private int pointer = 0;

    /**
     * The number of available bytes from the pointer to the end of the buffer.
     */
    private int available = 0;

    /**
     * Creates a new HTTP input stream.
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
     * Creates a new HTTP input stream.
     *
     * @param source the source stream
     */
    public HttpInputStream(final InputStream source) {
        this(source, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Estimates the number of bytes that can be read without blocking.
     *
     * @return the number of bytes than can be read without blocking
     *
     * @throws IOException if an I/O error occurs during this process
     */
    @Override
    public int available() throws IOException {
        return this.available - this.pointer + this.source.available();
    }

    /**
     * Closes the stream.
     *
     * @throws IOException if an I/O error occurs while closing the stream
     */
    @Override
    public void close() throws IOException {
        this.source.close();
        this.source = null;
        this.buffer = null;
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     * <p>
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
            return value & 255;
        }

        // Otherwise, try to load from the source stream.
        this.load();

        // Return EOF if the source stream says so.
        if (this.available == -1) {
            return -1;
        }

        // The buffer's fresh, get the next byte.
        this.pointer = 1;

        return this.buffer[0] & 255;
    }

    /**
     * Reads up to <code>length</code> bytes of data from the input stream into
     * an array of bytes.  An attempt is made to read as many as
     * <code>length</code> bytes, but a smaller number may be read.
     * The number of bytes actually read is returned as an integer.
     *
     * <p> This method blocks until input data is available, end of file is
     * detected, or an exception is thrown.
     *
     * <p> If <code>length</code> is zero, then no bytes are read and
     * <code>0</code> is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the stream is at end of
     * file, the value <code>-1</code> is returned; otherwise, at least one
     * byte is read and stored into <code>buffer</code>.
     *
     * <p> The first byte read is stored into element <code>buffer[offset]</code>, the
     * next one into <code>buffer[offset+1]</code>, and so on. The number of bytes read
     * is, at most, equal to <code>length</code>. Let <i>k</i> be the number of
     * bytes actually read; these bytes will be stored in elements
     * <code>buffer[offset]</code> through <code>buffer[offset+</code><i>k</i><code>-1]</code>,
     * leaving elements <code>buffer[offset+</code><i>k</i><code>]</code> through
     * <code>buffer[offset+length-1]</code> unaffected.
     *
     * <p> In every case, elements <code>buffer[0]</code> through
     * <code>buffer[offset]</code> and elements <code>buffer[offset+length]</code> through
     * <code>buffer[buffer.length-1]</code> are unaffected.
     *
     * <p> The <code>read(buffer,</code> <code>offset,</code> <code>length)</code> method
     * for class <code>InputStream</code> simply calls the method
     * <code>read()</code> repeatedly. If the first such call results in an
     * <code>IOException</code>, that exception is returned from the call to
     * the <code>read(buffer,</code> <code>offset,</code> <code>length)</code> method.  If
     * any subsequent call to <code>read()</code> results in a
     * <code>IOException</code>, the exception is caught and treated as if it
     * were end of file; the bytes read up to that point are stored into
     * <code>buffer</code> and the number of bytes read before the exception
     * occurred is returned. The default implementation of this method blocks
     * until the requested amount of input data <code>length</code> has been read,
     * end of file is detected, or an exception is thrown. Subclasses are encouraged
     * to provide a more efficient implementation of this method.
     *
     * @param      buffer the buffer into which the data is read.
     * @param      offset the start offset in array <code>b</code>
     *                    at which the data is written.
     * @param      length the maximum number of bytes to read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException If the first byte cannot be read for any reason
     * other than end of file, or if the input stream has been closed, or if
     * some other I/O error occurs.
     * @exception  NullPointerException If <code>buffer</code> is <code>null</code>.
     * @exception  IndexOutOfBoundsException If <code>offset</code> is negative,
     * <code>length</code> is negative, or <code>length</code> is greater than
     * <code>buffer.length - offset</code>
     * @see        java.io.InputStream#read()
     */
    @Override
    public int read(final byte[] buffer, final int offset, final int length) throws IOException {
        this.ensureOpen();

        // Sanitize the parameters.
        if (buffer == null) {
            throw new NullPointerException("The buffer is null.");
        }

        if (offset < 0) {
            throw new IndexOutOfBoundsException("The offset cannot be negative.");
        }

        if (length < 0) {
            throw new IndexOutOfBoundsException("The length cannot be negative.");
        }

        if (length > buffer.length - offset) {
            throw new IndexOutOfBoundsException("The length exceeds the array's bounds.");
        }

        // Return EOF if we know we reached that point.
        if (this.available == -1) {
            return -1;
        }

        // Determine the amount of bytes left in the buffer.
        final int bytesInBuffer = this.available - this.pointer;

        if (length <= bytesInBuffer) {
            System.arraycopy(this.buffer, this.pointer, buffer, offset, length);
            this.pointer += length;

            return length;
        }

        // Read the entire internal buffer into the output buffer.
        System.arraycopy(this.buffer, this.pointer, buffer, offset, bytesInBuffer);
        this.pointer = this.available;

        // Read the remaining from the source stream.
        int bytesLeftToRead = length - bytesInBuffer;
        int remainingOffset = offset + bytesInBuffer;
        final int numReadDirectly = this.source.read(buffer, remainingOffset, bytesLeftToRead);

        if (numReadDirectly >= 0) {
            // Return the total number of bytes read.
            return numReadDirectly + bytesInBuffer;
        } else {
            // The buffer has ended, return the bytes copied from the buffer.
            this.available = -1;

            if (bytesInBuffer > 0) {
                return bytesInBuffer;
            }

            return -1;
        }
    }

    /**
     * Reads a full line from the stream.
     *
     * <p>
     * The encoding is assumed to be ISO-8859-1, but only the US-ASCII interpretation can be
     * guaranteed.
     *
     * <p>
     * A line is delimited by a carriage return and a line feed, also denoted as "CRLF" or "\r\n".
     *
     * @return a single read line
     *
     * @throws IOException if an I/O error occurs while reading
     */
    public String readLine() throws IOException {
        this.ensureOpen();

        final StringBuilder lineBuilder = new StringBuilder();

        while (true) {
            // If we exhausted all characters, reload the buffer once more.
            if (this.pointer >= this.available) {
                this.load();
            }

            // If this is EOL, finish the string.
            if (this.available == -1) {
                return lineBuilder.toString();
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
                    ++this.pointer;
                    return lineBuilder.toString();
                }

                // It isn't, this character may be another CR, so start over from this point.
                // First push the just-read CR.
                lineBuilder.append('\r');
                continue;
            }

            lineBuilder.append((char) character);
        }
    }

    /**
     * Ensures that the source stream is open.
     *
     * @throws IOException if the source stream is closed
     */
    private void ensureOpen() throws IOException {
        if (this.source == null) {
            throw new IOException("The stream has been closed.");
        }
    }

    /**
     * Refreshes the buffer with new data from the source stream.
     *
     * <p>
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
