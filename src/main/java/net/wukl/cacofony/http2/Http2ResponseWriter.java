package net.wukl.cacofony.http2;

import net.wukl.cacofony.http.request.Header;
import net.wukl.cacofony.http.response.Response;
import net.wukl.cacofony.http2.frame.DataFrame;
import net.wukl.cacofony.http2.frame.Frame;
import net.wukl.cacofony.http2.frame.FrameFlag;
import net.wukl.cacofony.http2.stream.Stream;
import net.wukl.cacofony.util.CheckedExceptionTunnel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * A writer for HTTP/2 responses.
 */
public class Http2ResponseWriter {
    private static final Logger logger = LoggerFactory.getLogger(Http2ResponseWriter.class);

    /**
     * An array containing nothing.
     */
    private static final byte[] EMPTY_ARRAY = new byte[0];

    /**
     * Writes the response of an HTTP/2 request to the client.
     *
     * @param protocol the HTTP/2 protocol instance serving the connection
     * @param stream the stream the request was on
     * @param response the response the software generated
     *
     * @throws Exception if any error occurs
     */
    public void write(
            final Http2Protocol protocol, final Stream stream, final Response response
    ) throws Exception {
        this.writeHeaders(protocol, stream, response);

        final var streamId = stream.getId();
        final var bodyStream = new OutputStream() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void write(final int i) {
                this.write(new byte[] {(byte) i}, 0, 1);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void write(final byte[] b, final int off, final int len) {
                int written = 0;
                while (written < len) {
                    final var remainder = len - written;
                    final var payloadLength = Math.min(remainder, protocol.getMaxFrameSize());
                    final var payload = new byte[payloadLength];
                    System.arraycopy(b, off + written, payload, 0, payloadLength);

                    final var frame = new DataFrame(streamId, Collections.emptySet(), payload);
                    this.sendFrame(frame);

                    written += payloadLength;
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void close() {
                this.sendFrame(new DataFrame(
                        streamId, Collections.singleton(FrameFlag.END_STREAM), EMPTY_ARRAY
                ));
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return "HTTP/2 OutputStream writing response body to stream " + streamId;
            }

            /**
             * Sends any frame to the outbound queue of the connection.
             *
             * @param frame the frame
             */
            private void sendFrame(final Frame frame) {
                try {
                    protocol.enqueueOutbound(frame);
                } catch (final InterruptedException ex) {
                    throw new CheckedExceptionTunnel(ex);
                }
            }
        };

        response.write(bodyStream);
        bodyStream.close();
    }

    /**
     * Writes the response headers to the client.
     *
     * @param protocol the protocol instance serving the connection
     * @param stream the stream the request was on
     * @param response the response the software generated
     *
     * @throws InterruptedException
     *      if the outbound queue was full and the thread was interrupted while waiting for room
     *      in the queue
     */
    private void writeHeaders(
            final Http2Protocol protocol, final Stream stream, final Response response
    ) throws InterruptedException {
        final var headers = new ArrayList<Header>();
        headers.add(new Header(":status", String.valueOf(response.getStatus().getCode())));

        headers.addAll(response.getHeaders().entrySet().stream()
                        .map(e -> new Header(e.getKey(), e.getValue()))
                        .collect(Collectors.toList())
        );

        protocol.sendHeaders(stream.getId(), headers);
    }
}
