package net.wukl.cacofony.server;

import net.wukl.cacofony.http.exception.HttpException;
import net.wukl.cacofony.http.exception.SilentException;
import net.wukl.cacofony.http.request.MutableRequest;
import net.wukl.cacofony.http.request.Request;
import net.wukl.cacofony.http.request.RequestParser;
import net.wukl.cacofony.http.response.Response;
import net.wukl.cacofony.io.HttpInputStream;
import net.wukl.cacofony.io.ProtectedOutputStream;
import net.wukl.cacofony.server.host.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * A handler for active connections.
 *
 * @author Luc Everse
 */
public class ConnectionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);

    /**
     * The request parser to use.
     */
    private final RequestParser parser;

    /**
     * The hosts to serve.
     */
    private final Map<String, Host> hosts = new HashMap<>();

    /**
     * The default host if none matched.
     */
    private Host defaultHost;

    /**
     * Creates a new connection handler.
     *
     * @param parser the request parser to use
     */
    public ConnectionHandler(final RequestParser parser) {
        this.parser = parser;
    }

    /**
     * Sets the default host.
     *
     * @param host the host
     */
    public void setDefaultHost(final Host host) {
        this.defaultHost = host;
    }

    /**
     * Adds a host.
     *
     * @param host the host
     */
    public void addHost(final Host host) {
        this.hosts.put(host.getName(), host);
    }

    /**
     * Handles an active connection.
     *
     * @param conn the connection to handle
     */
    public void handle(final Connection conn) {
        try {
            logger.debug("Remote {} connected.", conn.getAddress());

            final ProtectedOutputStream out = new ProtectedOutputStream(conn.getOut());
            final HttpInputStream in = new HttpInputStream(conn.getIn());

            this.waitForRequest(conn, in, out);

            out.allowClosing(true);
            out.close();

            logger.debug("Remote {} disconnected.", conn.getAddress());
        } catch (final IOException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Connection reset by peer")) {
                logger.debug("Client closed connection.");
            } else {
                logger.error("I/O exception while serving a client: ", ex);
            }
        } catch (final Exception ex) {
            logger.error("Fatal exception: ", ex);
            throw ex;
        }
    }

    /**
     * The main request-response loop.
     *
     * @param conn the connection to serve
     * @param in the http input stream to use
     * @param out the protected output stream to use
     *
     * @throws IOException if an I/O error occurs
     */
    private void waitForRequest(
            final Connection conn, final HttpInputStream in, final ProtectedOutputStream out
    ) throws IOException {
        MutableRequest request;

        do {
            request = null;
            Response response;
            Host host = this.defaultHost;

            try {
                request = this.parser.parse(in);
                request.setPort(conn.getPort());
                request.setScheme(conn.getScheme());
                request.setRemote(conn.getAddress());

                final String hostname = request.getHost();
                host = this.hosts.getOrDefault(hostname, this.defaultHost);

                try {
                    response = host.getRouter().handle(request);
                } catch (final InvocationTargetException ex) {
                    // "Unpack" an exception raised through reflection calls.
                    throw ex.getCause();
                }

                host.getResponsePreparer().prepare(request, response);
            } catch (final SilentException ex) {
                logger.warn("Server closed connection. {}", ex.getMessage());
                return;
            } catch (final HttpException ex) {
                response = host.getExceptionHandler().handle(request, ex);
                host.getResponsePreparer().prepare(request, response);
            } catch (final IOException ex) {
                break;
            } catch (final Throwable ex) {
                response = host.getExceptionHandler().handle(request, ex);
                host.getResponsePreparer().prepare(request, response);
            }

            if (request != null) {
                logger.info("{} \"{} {} HTTP/{}.{}\" {} {}",
                        conn.getAddress(),
                        request.getMethod(),
                        request.getRawPath(),
                        request.getMajorVersion(),
                        request.getMinorVersion(),
                        response.getStatus().getCode(),
                        response.getContentLength());
            }

            final OutputStream stream = host.getResponseWriter().write(request, response, out);
            stream.close();
        } while (!this.mustCloseConnection(request));
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
