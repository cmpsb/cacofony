package net.wukl.cacofony.http2;

import net.wukl.cacofony.http.exception.SilentException;
import net.wukl.cacofony.http.request.Header;
import net.wukl.cacofony.http.request.Method;
import net.wukl.cacofony.http.request.MutableRequest;
import net.wukl.cacofony.http2.stream.Stream;
import net.wukl.cacofony.server.Connection;
import net.wukl.cacofony.server.host.HostMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * A handler for HTTP/2 requests.
 */
public class Http2RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(Http2RequestHandler.class);

    /**
     * The hosts the server is authoritative for.
     */
    private final HostMap hosts;

    /**
     * The thread pool to schedule requests on.
     */
    private final ExecutorService executor;

    /**
     * The HTTP/2 response writer.
     */
    private final Http2ResponseWriter writer;

    /**
     * Creates a new HTTP/2 request handler.
     *
     * @param hosts the hosts the server is authoritative for
     * @param executor the thread pool to schedule requests on
     * @param writer the HTTP/2 response writer
     */
    public Http2RequestHandler(
            final HostMap hosts,
            final ExecutorService executor,
            final Http2ResponseWriter writer
    ) {
        this.hosts = hosts;
        this.executor = executor;
        this.writer = writer;
    }

    /**
     * Handles a request.
     *
     * The requests are run in a separate thread, run in the {@link #executor},
     *
     * @param protocol the protocol instance the request was sent on
     * @param conn the connection the protocol instance is associated with
     * @param stream the particular stream the request was sent on
     *
     * @return the future representing the submitted request
     */
    public Future<?> handleRequest(
            final Http2Protocol protocol, final Connection conn, final Stream stream
    ) {
        return this.executor.submit(() -> {
            try {
                final var headers = new HashMap<String, Header>();
                stream.getHeaders().forEach(h -> headers.put(h.getKey(), h));

                final var request = new MutableRequest(
                        Method.get(headers.get(":method").getFirstValue()),
                        headers.get(":path").getFirstValue(),
                        2, 0
                );
                request.setScheme(headers.get(":scheme").getFirstValue());
                request.adoptHeaders(stream.getHeaders());
                request.setRemote(conn.getAddress());
                request.setBody(stream.getRequestPipe().getIn());

                final var contentLengthStr = request.getHeader("Content-Length");
                if (contentLengthStr != null) {
                    final var contentLength = Long.parseLong(contentLengthStr);
                    stream.setContentLength(contentLength);
                    if (contentLength == 0) {
                        stream.getRequestPipe().getOut().close();
                    }
                }

                final var host = this.hosts.get(request.getHost());
                final var response = host.handle(request);

                logger.info("{} \"{} {} HTTP/2\" {} {}",
                        conn.getAddress(),
                        request.getMethod(),
                        request.getRawPath(),
                        response.getStatus().getCode(),
                        response.getContentLength()
                );

                protocol.updateLastHandledStream(stream.getId());

                this.writer.write(protocol, stream, response);
            } catch (final SilentException ex) {
                logger.warn("Server closed connection: {}", ex.getMessage());
                protocol.trap();
            } catch (final Throwable t) {
                protocol.trap(t);
            }
        });
    }
}
