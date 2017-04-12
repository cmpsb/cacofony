package net.cmpsb.cacofony.http.response;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An abstract streamed response you can inherit from.
 * <p>
 * Extending this response allows you to stream bytes to the client without having to know
 * the full data size beforehand. This also means that the response will always use the
 * {@code chunked} transfer encoding. Compression can be applied too.
 *
 * @author Luc Everse
 */
public abstract class AbstractStreamedResponse extends Response {
    /**
     * Creates a new abstract streamed response.
     */
    public AbstractStreamedResponse() {

    }

    /**
     * Creates a new abstract streamed response.
     *
     * @param code the HTTP status code
     */
    public AbstractStreamedResponse(final ResponseCode code) {
        super(code);
    }

    /**
     * Writes the response body to the output stream.
     *
     * @param out the client's output stream
     * @throws IOException if an I/O error occurs during writing
     */
    @Override
    public abstract void write(OutputStream out) throws IOException;

    /**
     * Always returns {@code -1} to indicate that this response is a streamed response.
     *
     * @return {@code -1}
     */
    @Override
    public long getContentLength() {
        return -1;
    }
}
