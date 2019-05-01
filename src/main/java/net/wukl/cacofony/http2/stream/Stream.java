package net.wukl.cacofony.http2.stream;

import net.wukl.cacofony.http.request.Header;
import net.wukl.cacofony.http2.Window;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * An HTTP/2 stream.
 */
public class Stream implements AutoCloseable {
    /**
     * The stream identifier this instance is representing.
     */
    private final int id;

    /**
     * The buffer containing the full header block.
     */
    private ByteArrayOutputStream headerBytes = new ByteArrayOutputStream();

    /**
     * The pipe carrying the request payload.
     */
    private final StreamPipe requestPipe = new StreamPipe();

    /**
     * The headers the stream was opened with.
     */
    private List<Header> headers = null;

    /**
     * The flow control window applying to the current connection.
     */
    private final Window window;

    /**
     * The list of futures associated with the stream.
     */
    private final List<Future<?>> associatedFutures = new ArrayList<>();

    /**
     * The length of the content the client is willing to send on this stream.
     */
    private transient long contentLength = -1;

    /**
     * The number of bytes received from the client.
     */
    private transient long bytesReceived = 0;

    /**
     * Creates a new stream instance.
     *
     * @param id the identifier of the stream this instance represents
     * @param initLocalWindow the initial flow-control window for incoming frames
     * @param initRemoteWindow the initial flow-control window for outgoing frames
     */
    public Stream(final int id, final int initLocalWindow, final int initRemoteWindow) {
        this.id = id;
        this.window = new Window(initLocalWindow, initRemoteWindow);
    }

    /**
     * Returns the identifier of this stream.
     *
     * @return the identifier
     */
    public int getId() {
        return this.id;
    }

    /**
     * Adds header bytes to the stream's header byte buffer.
     *
     * This function is only valid as long as the full byte buffer has not been extracted yet using
     * {@link #getHeaderBytes()}.
     *
     * @param bytes the bytes to add
     *
     * @throws AssertionError if the function was called after {@link #getHeaderBytes()}
     */
    public void addHeaderBytes(final byte[] bytes) {
        assert this.headerBytes != null;

        this.headerBytes.writeBytes(bytes);
    }

    /**
     * Returns the byte array contained in the header byte buffer.
     *
     * The byte buffer is cleared after the bytes have been extracted, so this function is
     * <em>not</em> idempotent.
     *
     * @return the header bytes
     *
     * @throws AssertionError if the function was called for the second time on the same stream
     */
    public byte[] getHeaderBytes() {
        assert this.headerBytes != null;
        final var bytes = this.headerBytes.toByteArray();
        this.headerBytes = null;
        return bytes;
    }

    /**
     * Returns the pipe carrying the request payload.
     *
     * @return the request payload pipe
     */
    public StreamPipe getRequestPipe() {
        return this.requestPipe;
    }

    /**
     * Returns the flow control window.
     *
     * @return the window
     */
    public Window getWindow() {
        return this.window;
    }

    /**
     * Returns the headers that opened the stream.
     *
     * @return the headers
     */
    public List<Header> getHeaders() {
        return this.headers;
    }

    /**
     * Sets the headers that opened the stream.
     *
     * This function can be called only once.
     *
     * @param headers the headers
     *
     * @throws AssertionError if the function was called for the second time on the same stream
     */
    public void setHeaders(final List<Header> headers) {
        assert this.headers == null;
        assert headers != null;

        this.headers = headers;
    }

    /**
     * Associates a future with the stream.
     *
     * Associated futures will be cancelled if the client cancels the stream.
     *
     * @param future the future to associate
     */
    public void associateFuture(final Future<?> future) {
        this.associatedFutures.add(future);
    }

    /**
     * Sets the number of bytes the client has announced they intend to send to the server
     * over this stream.
     *
     * @param contentLength the number of bytes the client will send
     */
    public void setContentLength(final long contentLength) {
        this.contentLength = contentLength;
    }

    /**
     * Returns the number of bytes the client has announced they intend to send to the server
     * over this stream, or {@code -1} if the client will stream additional data instead.
     *
     * @return the number of bytes the client will send or -1
     */
    public long getContentLength() {
        return this.contentLength;
    }

    /**
     * Increases the number of bytes currently received on this stream.
     *
     * @param bytes the number of bytes to increase the count with
     */
    public void addBytesReceived(final long bytes) {
        this.bytesReceived += bytes;
    }

    /**
     * Returns the number of bytes currently received on this stream.
     *
     * @return the number of bytes received
     */
    public long getBytesReceived() {
        return this.bytesReceived;
    }

    /**
     * Checks whether the stream has received all bytes the client had announced they would send.
     *
     * If no content length was announced, this function returns {@code false}.
     *
     * @return {@code true} if all bytes have been received, {@code false} otherwise
     */
    public boolean hasReceivedAllBytes() {
        if (this.contentLength == -1) {
            return false;
        }

        return this.bytesReceived >= this.contentLength;
    }

    /**
     * Closes the stream.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        this.requestPipe.close();

        for (final var future : this.associatedFutures) {
            future.cancel(true);
        }
    }
}
