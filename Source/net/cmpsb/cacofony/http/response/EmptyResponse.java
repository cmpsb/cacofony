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
     * Writes nothing.
     * <p>
     * Does absolutely nothing, it doesn't even flush the stream.
     *
     * @param out the client's output stream
     * @throws IOException if an I/O error occurs during writing
     */
    @Override
    public final void write(final OutputStream out) throws IOException {

    }

    /**
     * Returns {@code 0}, since this response does not contain a body.
     *
     * @return {@code 0}
     */
    @Override
    public final long getContentLength() {
        return 0;
    }
}
