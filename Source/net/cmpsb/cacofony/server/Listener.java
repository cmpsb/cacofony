package net.cmpsb.cacofony.server;

import net.cmpsb.cacofony.di.Inject;
import net.cmpsb.cacofony.di.MultiInstance;
import net.cmpsb.cacofony.http.exception.HttpException;
import net.cmpsb.cacofony.http.exception.SilentException;
import net.cmpsb.cacofony.http.request.MutableRequest;
import net.cmpsb.cacofony.http.request.RequestParser;
import net.cmpsb.cacofony.http.response.ResponsePreparer;
import net.cmpsb.cacofony.http.response.TextResponse;
import net.cmpsb.cacofony.http.response.Response;
import net.cmpsb.cacofony.http.response.ResponseCode;
import net.cmpsb.cacofony.http.response.ResponseWriter;
import net.cmpsb.cacofony.io.HttpInputStream;
import net.cmpsb.cacofony.mime.MimeType;
import net.cmpsb.cacofony.route.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * A listener for incoming HTTP requests.
 *
 * @author Luc Everse
 */
@MultiInstance
public class Listener implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Listener.class);

    /**
     * The socket to listen on.
     */
    private final ServerSocket socket;

    /**
     * The executor service (or thread pool) to run the handler in.
     */
    private final ExecutorService executor;

    /**
     * The router to use.
     */
    private final Router router;

    /**
     * The request parser to use.
     */
    private final RequestParser parser;

    /**
     * The response writer to use.
     */
    private final ResponseWriter writer;

    /**
     *
     */
    private final ResponsePreparer preparer;

    /**
     * Creates a new listener.
     *
     * @param socket   the socket to listen on
     * @param executor the thread pool for incoming requests
     * @param router   the router to route requests through
     * @param parser   the request parser
     * @param writer   the request writer
     * @param preparer the response preparer to use
     */
    public Listener(@Inject("arg:socket")
                    final ServerSocket socket,
                    @Inject("name:resource/server.thread-pool")
                    final ExecutorService executor,
                    final Router router,
                    final RequestParser parser,
                    final ResponseWriter writer,
                    final ResponsePreparer preparer) {
        this.socket = socket;
        this.executor = executor;
        this.router = router;
        this.parser = parser;
        this.writer = writer;
        this.preparer = preparer;
    }

    /**
     * Run the listener.
     */
    @Override
    public void run() {
        logger.info("Now listening on port {}.", this.socket.getLocalPort());
        for (;;) {
            try {
                final Socket client = this.socket.accept();

                this.executor.submit(() -> this.handle(client));
            } catch (final IOException e) {
                logger.error("I/O exception while accepting a client: ", e);
            }
        }
    }

    /**
     * Handles an accepted request.
     *
     * @param client the client to serve
     */
    private void handle(final Socket client) {
        try {
            logger.debug("Remote {} connected.", client.getInetAddress());

            MutableRequest request = null;
            Response response;
            final OutputStream out = client.getOutputStream();

            try {
                final long start = System.nanoTime();

                final HttpInputStream in = new HttpInputStream(client.getInputStream());
                request = this.parser.parse(in);

                response = this.router.handle(request);

                final long time = System.nanoTime() - start;
                final double satisfactionIndex = 600_000_000.0 / time * 100.0;
                response.setHeader("X-Satisfaction-Index", satisfactionIndex + "%");

                this.preparer.prepare(request, response);
            } catch (final SilentException ex) {
                logger.warn("Server closed connection. {}", ex.getMessage());
                client.close();
                return;
            } catch (final HttpException ex) {
                logger.warn("HTTP exception: {}", ex.getMessage());
                response = new TextResponse(ex.getMessage());
                response.setContentType(MimeType.text());
                response.setStatus(ex.getCode());
                this.preparer.prepare(request, response);
            } catch (final Throwable t) {
                logger.error("Internal server error: ", t);
                response = new TextResponse("Internal Server Error");
                response.setContentType(MimeType.text());
                response.setStatus(ResponseCode.INTERNAL_SERVER_ERROR);
                this.preparer.prepare(request, response);
            }

            final OutputStream stream = this.writer.write(request, response, out);
            stream.close();
        } catch (final IOException ex) {
            logger.error("I/O exception while serving a client: ", ex);
        }
    }
}
