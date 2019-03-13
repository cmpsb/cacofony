package net.wukl.cacofony.http2.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * A pipe used to transport request data to and from the stream threads.
 */
public class StreamPipe implements AutoCloseable {
    /**
     * The output pipe.
     */
    private final PipedOutputStream out = new PipedOutputStream();

    /**
     * The input pipe.
     */
    private final PipedInputStream in = new PipedInputStream();

    /**
     * Creates a new stream pipe.
     */
    public StreamPipe() {
        try {
            this.out.connect(this.in);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns the output stream of the pipe.
     *
     * @return the output stream
     */
    public OutputStream getOut() {
        return this.out;
    }

    /**
     * Returns the input stream of the pipe.
     *
     * @return the input stream
     */
    public InputStream getIn() {
        return this.in;
    }

    /**
     * Closes the pipe.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        this.out.close();
    }
}
