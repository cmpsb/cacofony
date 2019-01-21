package net.wukl.cacofony.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that refuses to close until it has been confirmed that it can.
 *
 * @author Luc Everse
 */
public class ProtectedOutputStream extends OutputStream {
    /**
     * The target.
     */
    private OutputStream target;

    /**
     * Whether the stream should honor a {@link #close()} call.
     */
    private boolean mayClose = false;

    /**
     * Creates a new protected output stream.
     *
     * @param target the stream to write to
     */
    public ProtectedOutputStream(final OutputStream target) {
        this.target = target;
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     * The general contract for <code>write(b, off, len)</code> is that
     * some of the bytes in the array <code>b</code> are written to the
     * output stream in order; element <code>b[off]</code> is the first
     * byte written and <code>b[off+len-1]</code> is the last byte written
     * by this operation.
     * <p>
     * If <code>b</code> is <code>null</code>, a
     * <code>NullPointerException</code> is thrown.
     * <p>
     * If <code>off</code> is negative, or <code>len</code> is negative, or
     * <code>off+len</code> is greater than the length of the array
     * <code>b</code>, then an <tt>IndexOutOfBoundsException</tt> is thrown.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> is thrown if the output
     *             stream is closed.
     */
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        this.target.write(b, off, len);
    }

    /**
     * Flushes this output stream and forces any buffered output bytes
     * to be written out. The general contract of <code>flush</code> is
     * that calling it is an indication that, if any bytes previously
     * written have been buffered by the implementation of the output
     * stream, such bytes should immediately be written to their
     * intended destination.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void flush() throws IOException {
        this.target.flush();
    }

    /**
     * Writes the specified byte to this output stream. The general
     * contract for <code>write</code> is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument <code>b</code>. The 24
     * high-order bits of <code>b</code> are ignored.
     * <p>
     * Subclasses of <code>OutputStream</code> must provide an
     * implementation for this method.
     *
     * @param b the <code>byte</code>.
     * @throws IOException if an I/O error occurs. In particular,
     *                     an <code>IOException</code> may be thrown if the
     *                     output stream has been closed.
     */
    @Override
    public void write(final int b) throws IOException {
        this.target.write(b);
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with this stream, but only if it's allowed.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void close() throws IOException {
        if (this.mayClose) {
            this.target.close();
        }
    }

    /**
     * Allows or disallows the stream to close its target.
     *
     * @param allowed true to allow closing, false otherwise
     */
    public void allowClosing(final boolean allowed) {
        this.mayClose = allowed;
    }
}
