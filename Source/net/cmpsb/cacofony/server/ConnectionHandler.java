package net.cmpsb.cacofony.server;

import net.cmpsb.cacofony.di.Inject;
import net.cmpsb.cacofony.exception.ExceptionHandler;
import net.cmpsb.cacofony.http.exception.HttpException;
import net.cmpsb.cacofony.http.exception.SilentException;
import net.cmpsb.cacofony.http.request.MutableRequest;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.http.request.RequestParser;
import net.cmpsb.cacofony.http.response.Response;
import net.cmpsb.cacofony.http.response.ResponsePreparer;
import net.cmpsb.cacofony.http.response.ResponseWriter;
import net.cmpsb.cacofony.io.HttpInputStream;
import net.cmpsb.cacofony.io.ProtectedOutputStream;
import net.cmpsb.cacofony.route.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

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
    @Inject private RequestParser parser;

    /**
     * The router to run requests through.
     */
    @Inject private Router router;

    /**
     * The response preparer to use.
     */
    @Inject private ResponsePreparer preparer;

    /**
     * The request writer to use.
     */
    @Inject private ResponseWriter writer;

    /**
     * The exception handler to use.
     */
    @Inject private ExceptionHandler exceptionHandler;

    /**
     * Handles an active connection.
     *
     * @param client the client to serve
     * @param scheme the request scheme
     */
    public void handle(final Socket client, final String scheme) {
        try {
            logger.debug("Remote {} connected.", client.getInetAddress());

            final ProtectedOutputStream out = new ProtectedOutputStream(client.getOutputStream());
            final HttpInputStream in = new HttpInputStream(client.getInputStream());

            this.waitForRequest(client, out, in, scheme);

            out.allowClosing(true);
            out.close();

            logger.debug("Remote {} disconnected.", client.getInetAddress());
        } catch (final IOException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Connection reset by peer")) {
                logger.debug("Client closed connection.");
            } else {
                logger.error("I/O exception while serving a client: ", ex);
            }
        } catch (final RuntimeException ex) {
            logger.error("Fatal exception: ", ex);
            throw ex;
        }
    }

    /**
     * The main request-response loop.
     *
     * @param client the client
     * @param out    the client's target stream
     * @param in     the client's source stream
     * @param scheme the URI scheme
     *
     * @throws IOException if an I/O error occurs
     */
    private void waitForRequest(final Socket client,
                                final OutputStream out,
                                final HttpInputStream in,
                                final String scheme) throws IOException {
        MutableRequest request;

        do {
            request = null;
            Response response;

            try {
                request = this.parser.parse(in);
                request.setScheme(scheme);

                final long start = System.nanoTime();

                response = this.router.handle(request);

                final long time = System.nanoTime() - start;
                final double satisfactionIndex = 600_000_000.0 / time * 100.0;
                response.setHeader("X-Satisfaction-Index", satisfactionIndex + "%");

                this.preparer.prepare(request, response);
            } catch (final SilentException ex) {
                logger.warn("Server closed connection. {}", ex.getMessage());
                return;
            } catch (final HttpException ex) {
                response = this.exceptionHandler.handle(request, ex);
                this.preparer.prepare(request, response);
            } catch (final IOException ex) {
                break;
            } catch (final Exception ex) {
                response = this.exceptionHandler.handle(request, ex);
                this.preparer.prepare(request, response);
            }

            if (request != null) {
                logger.info("{} \"{} {} HTTP/{}.{}\" {} {}",
                        client.getInetAddress().getHostAddress(),
                        request.getMethod(),
                        request.getRawPath(),
                        request.getMajorVersion(),
                        request.getMinorVersion(),
                        response.getStatus().getCode(),
                        response.getContentLength());
            }

            final OutputStream stream = this.writer.write(request, response, out);
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
