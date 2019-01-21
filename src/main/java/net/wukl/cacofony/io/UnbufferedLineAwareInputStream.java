package net.wukl.cacofony.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * A wrapper around input streams that enables the stream to read lines,
 * but in an unbuffered manner.
 * <p>
 * Reading lines from this stream is an expensive operation.
 *
 * @author Luc Everse
 */
public class UnbufferedLineAwareInputStream extends LineAwareInputStream {
    /**
     * The source stream to read from.
     */
    private InputStream source;

    /**
     * Creates a new unbuffered line-aware input stream.
     *
     * @param source the source stream
     */
    public UnbufferedLineAwareInputStream(final InputStream source) {
        this.source = source;
    }

    /**
     * Skips over and discards <code>n</code> bytes of data from this input
     * stream. The <code>skip</code> method may, for a variety of reasons, end
     * up skipping over some smaller number of bytes, possibly <code>0</code>.
     * This may result from any of a number of conditions; reaching end of file
     * before <code>n</code> bytes have been skipped is only one possibility.
     * The actual number of bytes skipped is returned.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException  if the stream does not support seek,
     *                          or if some other I/O error occurs.
     */
    public long skip(final long n) throws IOException {
        this.ensureOpen();
        return this.source.skip(n);
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or
     * skipped over) from this input stream without blocking by the next
     * invocation of a method for this input stream. The next invocation
     * might be the same thread or another thread.  A single read or skip of this
     * many bytes will not block, but may read or skip fewer bytes.
     *
     * <p> Note that while some implementations of {@code InputStream} will return
     * the total number of bytes in the stream, many will not.  It is
     * never correct to use the return value of this method to allocate
     * a buffer intended to hold all data in this stream.
     *
     * @return     an estimate of the number of bytes that can be read (or skipped
     *             over) from this input stream without blocking or {@code 0} when
     *             it reaches the end of the input stream.
     * @exception  IOException if an I/O error occurs.
     */
    @Override
    public int available() throws IOException {
        this.ensureOpen();
        return this.source.available();
    }

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void close() throws IOException {
        this.source.close();
        this.source = null;
    }

    /**
     * Marks the current position in this input stream. A subsequent call to
     * the <code>reset</code> method repositions this stream at the last marked
     * position so that subsequent reads re-read the same bytes.
     *
     * <p> The <code>readlimit</code> arguments tells this input stream to
     * allow that many bytes to be read before the mark position gets
     * invalidated.
     *
     * <p> The general contract of <code>mark</code> is that, if the method
     * <code>markSupported</code> returns <code>true</code>, the stream somehow
     * remembers all the bytes read after the call to <code>mark</code> and
     * stands ready to supply those same bytes again if and whenever the method
     * <code>reset</code> is called.  However, the stream is not required to
     * remember any data at all if more than <code>readlimit</code> bytes are
     * read from the stream before <code>reset</code> is called.
     *
     * @param   readlimit   the maximum limit of bytes that can be read before
     *                      the mark position becomes invalid.
     * @see     java.io.InputStream#reset()
     */
    public synchronized void mark(final int readlimit) {
        if (this.source != null) {
            this.source.mark(readlimit);
        }
    }

    /**
     * Tests if this input stream supports the <code>mark</code> and
     * <code>reset</code> methods. Whether or not <code>mark</code> and
     * <code>reset</code> are supported is an invariant property of a
     * particular input stream instance.
     *
     * @return  <code>true</code> if this stream instance supports the mark
     *          and reset methods; <code>false</code> otherwise.
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     */
    public boolean markSupported() {
        return this.source != null && this.source.markSupported();
    }

    /**
     * Repositions this stream to the position at the time the
     * <code>mark</code> method was last called on this input stream.
     *
     * <p> The general contract of <code>reset</code> is:
     *
     * <ul>
     * <li> If the method <code>markSupported</code> returns
     * <code>true</code>, then:
     *
     *     <ul><li> If the method <code>mark</code> has not been called since
     *     the stream was created, or the number of bytes read from the stream
     *     since <code>mark</code> was last called is larger than the argument
     *     to <code>mark</code> at that last call, then an
     *     <code>IOException</code> might be thrown.
     *
     *     <li> If such an <code>IOException</code> is not thrown, then the
     *     stream is reset to a state such that all the bytes read since the
     *     most recent call to <code>mark</code> (or since the start of the
     *     file, if <code>mark</code> has not been called) will be resupplied
     *     to subsequent callers of the <code>read</code> method, followed by
     *     any bytes that otherwise would have been the next input data as of
     *     the time of the call to <code>reset</code>. </ul>
     *
     * <li> If the method <code>markSupported</code> returns
     * <code>false</code>, then:
     *
     *     <ul><li> The call to <code>reset</code> may throw an
     *     <code>IOException</code>.
     *
     *     <li> If an <code>IOException</code> is not thrown, then the stream
     *     is reset to a fixed state that depends on the particular type of the
     *     input stream and how it was created. The bytes that will be supplied
     *     to subsequent callers of the <code>read</code> method depend on the
     *     particular type of the input stream. </ul></ul>
     *
     * <p>The method <code>reset</code> for class <code>InputStream</code>
     * does nothing except throw an <code>IOException</code>.
     *
     * @exception  IOException  if this stream has not been marked or if the
     *               mark has been invalidated.
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.IOException
     */
    public synchronized void reset() throws IOException {
        this.ensureOpen();
        this.source.reset();
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     * <p> A subclass must provide an implementation of this method.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     * stream is reached.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        this.ensureOpen();
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
        return this.source.read(buffer, offset, length);
    }

    /**
     * Reads a full line from the stream.
     * <p>
     * The character encoding used is ISO-8859-1.
     * <p>
     * A line is delimited by a carriage return and a line feed, or CRLF.
     *
     * @return a single line
     * @throws IOException if an I/O error occurs while reading
     */
    @Override
    public String readLine() throws IOException {
        this.ensureOpen();

        final StringBuilder lineBuilder = new StringBuilder();

        while (true) {
            // Read the next character.
            int character = this.source.read();

            // Terminate the string if this is EOF.
            if (character == -1) {
                return lineBuilder.toString();
            }

            // If the next character is a CR, check if the one after that is an LF.
            while (character == '\r') {
                final int possibleLf = this.source.read();

                // If the stream ends here, return the string including the CR.
                if (possibleLf == -1) {
                    lineBuilder.append((char) character);
                    return lineBuilder.toString();
                }

                // If it is an LF, return the built string.
                if (possibleLf == '\n') {
                    return lineBuilder.toString();
                }

                // Otherwise push the previous character and check again.
                lineBuilder.append((char) character);
                character = possibleLf;
            }

            // Push the last-read character.
            lineBuilder.append((char) character);
        }
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
}
