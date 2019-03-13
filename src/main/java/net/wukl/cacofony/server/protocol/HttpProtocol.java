package net.wukl.cacofony.server.protocol;

import net.wukl.cacofony.http.exception.SilentException;
import net.wukl.cacofony.http.request.MutableRequest;
import net.wukl.cacofony.http.request.Request;
import net.wukl.cacofony.http.request.RequestParser;
import net.wukl.cacofony.http.response.Response;
import net.wukl.cacofony.io.HttpInputStream;
import net.wukl.cacofony.io.ProtectedOutputStream;
import net.wukl.cacofony.server.Connection;
import net.wukl.cacofony.server.host.Host;
import net.wukl.cacofony.server.host.HostMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

/**
 * A protocol supporting HTTP/1.0 and HTTP/1.1.
 */
public class HttpProtocol implements Protocol {
    private static final Logger logger = LoggerFactory.getLogger(HttpProtocol.class);

    /**
     * The hosts the protocol is serving.
     */
    private final HostMap hosts;

    /**
     * The request parser to use.
     */
    private final RequestParser parser;

    /**
     * The connection the protocol runs over.
     */
    private final Connection conn;

    /**
     * The protected output stream to the client.
     */
    private final ProtectedOutputStream out;

    /**
     * The HTTP input stream from the client.
     */
    private final HttpInputStream in;

    /**
     * Creates a new HTTP protocol instance.
     *
     * @param conn the connection the protocol will talk over
     * @param hosts the hosts the protocol can serve
     * @param parser the HTTP request parser to use
     */
    public HttpProtocol(final Connection conn, final HostMap hosts, final RequestParser parser) {
        this.conn = conn;
        this.hosts = hosts;
        this.parser = parser;

        this.out = new ProtectedOutputStream(conn.getOut());
        this.in = new HttpInputStream(conn.getIn());
    }

    /**
     * Returns the name of the protocol.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return "http/1.x";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Protocol handle() throws Throwable {
        MutableRequest request = null;
        Host host = null;

        Response response;

        try {
            request = this.parser.parse(this.in);
            request.setPort(this.conn.getPort());
            request.setScheme(this.conn.getScheme());
            request.setRemote(this.conn.getAddress());

            final String hostname = request.getHost();
            host = this.hosts.get(hostname);
            response = host.handle(request);
        } catch (final SilentException ex) {
            logger.warn("Server closed connection. {}", ex.getMessage());
            return null;
        }

        if (request != null) {
            logger.info("{} \"{} {} HTTP/{}.{}\" {} {}",
                    this.conn.getAddress(),
                    request.getMethod(),
                    request.getRawPath(),
                    request.getMajorVersion(),
                    request.getMinorVersion(),
                    response.getStatus().getCode(),
                    response.getContentLength());
        }

        final OutputStream stream = host.getResponseWriter().write(request, response, this.out);
        stream.close();

        final var mustClose = this.mustCloseConnection(request);

        if (mustClose) {
            this.out.allowClosing(true);
            this.out.close();
            return null;
        }

        return this;
    }

    /**
     * Returns whether to close the connection after serving a request.
     *
     * @param request the request to examine
     *
     * @return true if the connection must close, false otherwise
     */
    private boolean mustCloseConnection(final Request request) {
        if (request == null) {
            return true;
        }

        int major = request.getMajorVersion();
        int minor = request.getMinorVersion();
        final String connection = request.getHeader("Connection", "__Cacofony_unspecified__");

        return (major == 1 && minor == 0 && !connection.equalsIgnoreCase("keep-alive"))
                || (major == 1 && minor == 1 && connection.equalsIgnoreCase("close"));
    }
}
