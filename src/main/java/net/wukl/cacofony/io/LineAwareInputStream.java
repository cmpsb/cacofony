package net.wukl.cacofony.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Marks an input stream HTTP line-aware.
 *
 * <p>
 * A stream that's line-aware can read individual lines delimited by CRLF.
 *
 * @author Luc Everse
 */
public abstract class LineAwareInputStream extends InputStream {
    /**
     * Reads a full line from the stream.
     *
     * <p>
     * The character encoding used is ISO-8859-1.
     *
     * <p>
     * A line is delimited by a carriage return and a line feed, or CRLF.
     *
     * @return a single line
     *
     * @throws IOException if an I/O error occurs while reading
     */
    public abstract String readLine() throws IOException;
}
