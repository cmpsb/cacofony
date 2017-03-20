package net.cmpsb.cacofony.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * A decoder for chunked HTTP message bodies.
 *
 * @author Luc Everse
 */
public class ChunkedBodyReader extends Reader {
    /**
     * The source reader to read the data from.
     */
    private final Reader source;

    /**
     * Create a new chunked body reader.
     *
     * @param reader the source reader
     */
    public ChunkedBodyReader(final Reader reader) {
        this.source = reader;
    }

    /**
     * Reads characters into a portion of an array.  This method will block
     * until some input is available, an I/O error occurs, or the end of the
     * stream is reached.
     *
     * @param cbuf Destination buffer
     * @param off  Offset at which to start storing characters
     * @param len  Maximum number of characters to read
     * @return The number of characters read, or -1 if the end of the
     * stream has been reached
     * @throws IOException If an I/O error occurs
     */
    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        return 0;
    }

    /**
     * Closes the stream and releases any system resources associated with
     * it.  Once the stream has been closed, further read(), ready(),
     * mark(), reset(), or skip() invocations will throw an IOException.
     * Closing a previously closed stream has no effect.
     *
     * @throws IOException If an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        // Does nothing.
    }

    /**
     * Load the next chunk from
     */
    private void loadNextChunk() {

    }
}
