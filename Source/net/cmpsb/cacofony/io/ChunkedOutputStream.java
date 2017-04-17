package net.cmpsb.cacofony.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * An output stream that writes the data in chunks.
 *
 * @author Luc Everse
 */
public class ChunkedOutputStream extends OutputStream {
    /**
     * A CRLF byte array.
     */
    private static final byte[] NEWLINE = {'\r', '\n'};

    /**
     * The terminating 0-length chunk.
     */
    private static final byte[] TERMINATOR = {'0', '\r', '\n', '\r', '\n'};

    /**
     * The target output stream to write the chunks to.
     */
    private OutputStream target;

    /**
     * The internal buffer chunks are written to.
     */
    private byte[] buffer;

    /**
     * The size of the internal buffer as a hex string in bytes.
     */
    private byte[] bufferSizeInHex;

    /**
     * The pointer into the buffer where the next write will happen.
     */
    private int pointer = 0;

    /**
     * Whether the stream has ended or not.
     */
    private boolean eos = false;

    /**
     * Creates a new chunked output stream with a specific buffer size.
     * <p>
     * The {@code size} parameter specifies the maximum size a sent chunk may have. The stream will
     * buffer bytes up to that amount.
     *
     * @param target the stream to write the chunks to
     * @param size   the size, in bytes, of the chunk buffer
     */
    public ChunkedOutputStream(final OutputStream target, final int size) {
        if (size <= 0) {
            throw new IllegalArgumentException(
                    "The buffer size must be a positive (non-zero) integer."
            );
        }

        this.buffer = new byte[size];
        this.bufferSizeInHex = Integer.toHexString(size).getBytes(StandardCharsets.ISO_8859_1);
        this.target = target;
    }

    /**
     * Creates a new chunked output stream with the default buffer size.
     * <p>
     * Currently the default size is 8192 bytes.
     *
     * @param target the stream to write the chunks to
     */
    public ChunkedOutputStream(final OutputStream target) {
        this(target, 8192);
    }

    /**
     * Writes the specified byte to this output stream. The general
     * contract for <code>write</code> is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument <code>b</code>. The 24
     * high-order bits of <code>b</code> are ignored.
     *
     * @param b the <code>byte</code>.
     * @throws IOException if an I/O error occurs. In particular,
     *                     an <code>IOException</code> may be thrown if the
     *                     output stream has been closed.
     */
    @Override
    public void write(final int b) throws IOException {
        this.ensureOpen();

        if (this.pointer >= this.buffer.length) {
            this.flush();
        }

        this.buffer[this.pointer] = (byte) b;
        ++this.pointer;
    }

    /**
     * Writes {@code length} bytes from the specified byte array starting at offset {@code offset}
     * to this output stream
     * <p>
     * If {@code data} is {@code null}, a  {@code NullPointerException} is thrown.
     * <p>
     * If {@code offset} is negative, {@code length} is negative, or {@code offset+length} is
     * greater than the length of the array {@code data}, then an
     * {@code IndexOutOfBoundsException} is thrown.
     *
     * @param data   the buffer
     * @param offset the start offset in the buffer
     * @param length the number of bytes to write
     *
     * @throws IOException if an I/O error occurs. In particular,
     *                     an <code>IOException</code> may be thrown if the
     *                     output stream has been closed.
     */
    @Override
    public void write(final byte[] data, final int offset, final int length) throws IOException {
        this.ensureOpen();

        // Check the parameters for shenanigans.
        if (data == null) {
            throw new NullPointerException("The buffer is null.");
        }

        if (offset < 0) {
            throw new IndexOutOfBoundsException("The offset is negative.");
        }

        if (length < 0) {
            throw new IndexOutOfBoundsException("The length is negative.");
        }

        if (offset + length > data.length) {
            throw new IndexOutOfBoundsException(
                    "The bytes to write are (partly) outside the given buffer."
            );
        }

        final int bytesLeftInBuffer = this.buffer.length - this.pointer;

        // If there is enough room left in the buffer, just copy and exit.
        if (length < bytesLeftInBuffer) {
            System.arraycopy(data, offset, this.buffer, this.pointer, length);
            this.pointer += length;
            return;
        }

        // Otherwise copy what's possible.
        System.arraycopy(data, offset, this.buffer, this.pointer, bytesLeftInBuffer);
        this.pointer = this.buffer.length;
        this.flush();

        // Keep generating chunks while the data can't fit in the internal buffer.
        int sourceOffset = offset + bytesLeftInBuffer;
        int leftToWrite = length - bytesLeftInBuffer;
        while (leftToWrite > this.buffer.length) {
            this.target.write(this.bufferSizeInHex);
            this.target.write(NEWLINE);
            this.target.write(data, sourceOffset, this.buffer.length);
            this.target.write(NEWLINE);
            this.target.flush();

            sourceOffset += this.buffer.length;
            leftToWrite  -= this.buffer.length;
        }

        // Put the remaining part in the buffer.
        System.arraycopy(data, sourceOffset, this.buffer, 0, leftToWrite);
        this.pointer = leftToWrite;
    }

    /**
     * Flushes this output stream and forces a new chunk to be generated.
     * <p>
     * 0-byte flushes are ignored; to end the stream, use {@link #close()}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void flush() throws IOException {
        this.ensureOpen();

        if (this.pointer <= 0) {
            return;
        }

        final String length = Integer.toHexString(this.pointer);
        this.target.write(length.getBytes(StandardCharsets.ISO_8859_1));
        this.target.write(NEWLINE);
        this.target.write(this.buffer, 0, this.pointer);
        this.target.write(NEWLINE);
        this.target.flush();

        this.pointer = 0;
    }

    /**
     * Closes the stream.
     * <p>
     * The client will be notified of this fact by sending it a 0-length chunk.
     * No extra headers are appended.
     * <p>
     * Under normal circumstances in a request-response lifecycle, this method will flush all its
     * buffers and close any underlying streams, but the client socket will stay open. Closing the
     * socket is handled by the {@link net.cmpsb.cacofony.server.ConnectionHandler}. If you do want
     * to close the entire socket, throw a
     * {@link net.cmpsb.cacofony.http.exception.SilentException}. Do note however that this also
     * causes any response data to be ignored.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        this.ensureOpen();

        this.flush();

        this.target.write(TERMINATOR);

        this.target.close();

        this.target = null;
        this.buffer = null;
        this.bufferSizeInHex = null;
    }

    /**
     * Ensures that the stream is open.
     *
     * @throws IOException if the stream has been closed
     */
    private void ensureOpen() throws IOException {
        if (this.target == null) {
            throw new IOException("The stream has been closed.");
        }
    }
}
