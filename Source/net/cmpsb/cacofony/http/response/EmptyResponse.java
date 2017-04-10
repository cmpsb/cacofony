package net.cmpsb.cacofony.http.response;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A response that has no body.
 *
 * @author Luc Everse
 */
public class EmptyResponse extends Response {
    /**
     * Creates a new empty response with a status code.
     *
     * @param code the status code
     */
    public EmptyResponse(final ResponseCode code) {
        super(code);
    }

    /**
     * Creates a new empty response.
     */
    public EmptyResponse() {
        super();
    }

    /**
     * Writes the response body to the output stream.
     *
     * @param out the client's output stream
     * @throws IOException if an I/O error occurs during writing
     */
    @Override
    public void write(final OutputStream out) throws IOException {

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
        return 0;
    }
}
