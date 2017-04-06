package net.cmpsb.cacofony.http.response;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Luc Everse
 */
public class StreamedResponse extends Response {
    /**
     * The callback to call.
     */
    private Callback callback;

    /**
     * Creates a new streamed response with a status code.
     *
     * @param status   the status code
     * @param callback the callback that writes the data
     */
    public StreamedResponse(final ResponseCode status, final Callback callback) {
        super(status);
        this.callback = callback;
    }

    /**
     * Creates a new streamed response.
     *
     * @param callback the callback that writes the data
     */
    public StreamedResponse(final Callback callback) {
        this.callback = callback;
    }

    /**
     * Writes the response body to the output stream.
     *
     * @param out the client's output stream
     * @throws IOException if an I/O error occurs during writing
     */
    @Override
    public void write(final OutputStream out) throws IOException {
        this.callback.write(out);
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

    /**
     * The callback the user should implement.
     */
    @FunctionalInterface
    public interface Callback {
        /**
         * Writes some data to the output stream.
         *
         * @param out the stream to write to
         *
         * @throws IOException if an I/O error occurs
         */
        void write(OutputStream out) throws IOException;
    }
}
