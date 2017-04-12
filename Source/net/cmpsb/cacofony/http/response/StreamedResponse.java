package net.cmpsb.cacofony.http.response;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A response that streams data generated by a callback.
 * <p>
 * If you intend to send a file, see the {@link net.cmpsb.cacofony.http.response.file.FileResponse}
 * instead.
 * <p>
 * Consider extending {@link AbstractStreamedResponse} instead. This class is only a wrapper
 * that provides a lambda interface, which may incur a slight performance cost. (Though that is
 * negligible).
 * <p>
 * The response is always sent with the {@code chunked} transfer encoding.
 *
 * @author Luc Everse
 */
public class StreamedResponse extends AbstractStreamedResponse {
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
