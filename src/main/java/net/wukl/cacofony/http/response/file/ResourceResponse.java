package net.wukl.cacofony.http.response.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A response that can send .jar resources.
 *
 * @author Luc Everse
 */
public class ResourceResponse extends CachableResponse {
    /**
     * The stream to read the resource from.
     */
    private final InputStream in;

    /**
     * The size, in bytes, of the buffer used to transfer the file.
     */
    private int bufferSize = 8192;

    /**
     * Creates a new response sending a resource.
     *
     * @param jar          the class used to refer to the resource
     * @param resource     the path to the resource
     * @param lastModified the datetime the resource was last modified
     */
    public ResourceResponse(final Class<?> jar, final String resource, final long lastModified) {
        super(lastModified);
        this.in = jar.getResourceAsStream(resource);
    }

    /**
     * Sets the buffer size.
     *
     * @param bufferSize the size, in bytes, of the output buffer
     *
     * @throws IllegalArgumentException if the buffer size is non-positive
     */
    public void setBufferSize(final int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size is non-positive.");
        }

        this.bufferSize = bufferSize;
    }

    /**
     * Writes the response body to the output stream.
     *
     * @param out the client's output stream
     * @throws IOException if an I/O error occurs during writing
     */
    @Override
    public void write(final OutputStream out) throws IOException {
        final byte[] buffer = new byte[this.bufferSize];

        while (true) {
            final int read = this.in.read(buffer);

            if (read == -1) {
                break;
            }

            out.write(buffer, 0, read);
        }
    }

    /**
     * Calculates the length, in bytes, of the data to send.
     * <p>
     * If {@code -1}, then a collection of transfer encodings are applied. This allows for
     * big responses that don't fit entirely within memory.
     *
     * @return the length, in bytes, of the data to send or {@code -1} if it's unknown
     */
    @Override
    public long getContentLength() {
        return -1;
    }
}
