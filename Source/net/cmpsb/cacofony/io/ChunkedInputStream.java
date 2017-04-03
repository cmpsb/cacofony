package net.cmpsb.cacofony.io;

import net.cmpsb.cacofony.http.exception.BadRequestException;
import net.cmpsb.cacofony.http.exception.SilentException;
import net.cmpsb.cacofony.http.request.HeaderParser;
import net.cmpsb.cacofony.http.request.MutableRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * An input stream reading HTTP chunks.
 *
 * @author Luc Everse
 */
public class ChunkedInputStream extends InputStream {
    /**
     * A regex to split chunk extensions.
     */
    private static final String CHUNK_EXT_SPLIT_REGEX = ";";

    /**
     * A pattern to split chunk extensions.
     */
    private static final Pattern CHUNK_EXT_SPLIT_PATTERN = Pattern.compile(CHUNK_EXT_SPLIT_REGEX);

    /**
     * The source stream to read from.
     */
    private LineAwareInputStream source;

    /**
     * The request this stream is reading for.
     */
    private final MutableRequest request;

    /**
     * The header parser to use.
     */
    private final HeaderParser headerParser;

    /**
     * The number of octets left in the chunk.
     */
    private int bytesLeftInChunk = 0;

    /**
     * Set as soon as the last chunk was received.
     */
    private boolean eof = false;

    /**
     * Cleared as soon as the first chunk has been read.
     */
    private boolean firstChunk = true;

    /**
     * Creates a new chunked input stream.
     *
     * @param source       the source stream to read from
     * @param request      the request the stream is reading for
     * @param headerParser the header parser to use
     */
    public ChunkedInputStream(final LineAwareInputStream source,
                              final MutableRequest request,
                              final HeaderParser headerParser) {
        this.source = source;
        this.request = request;
        this.headerParser = headerParser;
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     * stream is reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        this.ensureOpen();

        // Return EOF if the last chunk was read.
        if (this.eof) {
            return -1;
        }

        // Read the next part of the chunk if there's not enough data left.
        if (this.bytesLeftInChunk <= 0) {
            this.readChunk();
        }

        --this.bytesLeftInChunk;

        return this.source.read();
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

        // Make sure the buffer is valid.
        if (buffer == null) {
            throw new NullPointerException("The buffer is null.");
        }

        // And the offset.
        if (offset < 0) {
            throw new IndexOutOfBoundsException("The offset cannot be negative.");
        }

        // And the length.
        if (length < 0) {
            throw new IndexOutOfBoundsException("The length cannot be negative.");
        }

        // And the length, but now for the given buffer.
        if (length > buffer.length - offset) {
            throw new IndexOutOfBoundsException("The length exceeds the array's bounds.");
        }

        // Return EOF if the stream has ended.
        if (this.eof) {
            return 0;
        }

        int leftToRead = length;
        int total = 0;

        // Keep reading chunks until less bytes-to-read remain than there are in the current chunk.
        while (leftToRead > this.bytesLeftInChunk) {
            // Stop reading if this is EOF.
            if (this.eof) {
                return total;
            }

            // Read some of the bytes.
            final int justRead = this.source.read(buffer, offset + total, this.bytesLeftInChunk);

            if (justRead == -1) {
                throw new SilentException(
                        "Reached EOF while still expecting bytes from a chunked transfer."
                );
            }

            // Update the totals and the number of bytes left to read.
            total += justRead;
            leftToRead -= justRead;

            // Move on to the next chunk.
            this.readChunk();
        }

        final int justRead = this.source.read(buffer, offset + total, leftToRead);

        if (justRead < leftToRead) {
            throw new SilentException(
                    "Reached EOF while still expecting bytes from a chunked transfer."
            );
        }

        // Update how many bytes are left for that chunk and the total amount of bytes read.
        this.bytesLeftInChunk -= justRead;
        total += justRead;

        return total;
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or
     * skipped over) from this input stream without blocking by the next
     * invocation of a method for this input stream. The next invocation
     * might be the same thread or another thread.  A single read or skip of this
     * many bytes will not block, but may read or skip fewer bytes.
     *
     * <p> This implementation will always return the number of bytes left unread in the current
     * chunk. More chunks may follow.
     *
     * @return     an estimate of the number of bytes that can be read (or skipped
     *             over) from this input stream without blocking or {@code 0} when
     *             it reaches the end of the input stream.
     * @exception  IOException if an I/O error occurs.
     */
    @Override
    public int available() throws IOException {
        this.ensureOpen();

        return this.bytesLeftInChunk;
    }

    /**
     * Skips over and discards <code>n</code> bytes of data from this input
     * stream. The <code>skip</code> method may, for a variety of reasons, end
     * up skipping over some smaller number of bytes, possibly <code>0</code>.
     * This may result from any of a number of conditions; reaching end of file
     * before <code>n</code> bytes have been skipped is only one possibility.
     * The actual number of bytes skipped is returned.
     *
     * This function will always try to skip the requested amount; the only way this may skip
     * less is when the source stream signals an EOF while skipping.
     *
     * @param      bytes   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException  if the stream does not support seek,
     *                          or if some other I/O error occurs.
     */
    @Override
    public long skip(final long bytes) throws IOException {
        this.ensureOpen();

        // Sanitize the length to skip.
        if (bytes <= 0 || this.eof) {
            return 0;
        }

        long bytesLeftToSkip = bytes;
        long bytesSkipped = 0;

        // While the amount to skip is greater than the amount in the current chunk,
        // load the next.
        while (bytesLeftToSkip > this.bytesLeftInChunk) {
            // Keep skipping bytes in the source stream until the end of the current chunk.
            long skippedWithinChunk = 0;
            while (skippedWithinChunk < this.bytesLeftInChunk) {
                final long skippedThisIteration =
                        this.source.skip(this.bytesLeftInChunk - skippedWithinChunk);

                if (skippedThisIteration == 0) {
                    throw new SilentException(
                            "Reached EOF while still expecting bytes from a chunked transfer."
                    );
                }

                skippedWithinChunk += skippedThisIteration;
            }

            bytesSkipped += this.bytesLeftInChunk;
            bytesLeftToSkip -= this.bytesLeftInChunk;

            this.readChunk();

            // Stop skipping if we've reached EOF.
            if (this.eof) {
                return bytesSkipped;
            }

        }

        this.bytesLeftInChunk -= bytesLeftToSkip;

        long skippedInSource = 0;
        while (skippedInSource < bytesLeftToSkip) {
            final long thisIteration = this.source.skip(bytesLeftToSkip);
            if (thisIteration == 0) {
                throw new SilentException(
                        "Reached EOF while still expecting bytes from a chunked transfer."
                );
            }

            skippedInSource += thisIteration;
        }

        // All requested bytes have been skipped.
        return bytes;
    }

    /**
     * Reads the next chunk's metadata.
     *
     * @throws IOException if an I/O error occurs while reading
     */
    private void readChunk() throws IOException {
        // If needed, read the last chunk's tail.
        if (!this.firstChunk) {
            this.source.readLine();
        }

        // Read the chunk header and split it into parts.
        final String chunkHeader = this.source.readLine();
        final String[] parts = CHUNK_EXT_SPLIT_PATTERN.split(chunkHeader);

        // Parse the reported length.
        final int size;
        try {
            size = Integer.parseInt(parts[0], 16);
        } catch (final NumberFormatException ex) {
            throw new BadRequestException("Unparsable chunk size.", ex);
        }

        // Check for an invalid size.
        if (size < 0) {
            throw new BadRequestException("Negative chunk size.");
        }

        // If the reported size is 0 there's no more chunks left to process.
        // Process some of the trailing headers.
        if (size == 0) {
            this.eof = true;
            this.headerParser.parse(this.source, this.request);
        }

        // Register the just-read information and remember that at least one chunk was read.
        this.bytesLeftInChunk = size;
        this.firstChunk = false;
    }

    /**
     * Ensures that the stream is open.
     *
     * @throws IOException if the stream has been closed
     */
    private void ensureOpen() throws IOException {
        if (this.source == null) {
            throw new IOException("The stream has been closed.");
        }
    }

    /**
     * Closes the input stream.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        this.source.close();
        this.source = null;
    }
}
