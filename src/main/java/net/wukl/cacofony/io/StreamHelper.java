package net.wukl.cacofony.io;

import java.io.InputStream;

/**
 * A helper class for stream operations.
 *
 * @author Luc Everse
 */
public class StreamHelper {
    /**
     * If a stream is not line-aware, wrap it into an unbuffered wrapper.
     *
     * @param source the stream to (possible) wrap
     *
     * @return a line-aware input stream
     */
    public LineAwareInputStream makeLineAware(final InputStream source) {
        if (source instanceof LineAwareInputStream) {
            return (LineAwareInputStream) source;
        }

        return new UnbufferedLineAwareInputStream(source);
    }
}
