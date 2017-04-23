package net.cmpsb.cacofony.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * A listener for incoming HTTP requests.
 *
 * @author Luc Everse
 */
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
     * The connection handler to use.
     */
    private final ConnectionHandler handler;

    /**
     * The connection scheme this listener uses.
     */
    private final String scheme;

    /**
     * Creates a new listener.
     *
     * @param socket   the socket to listen on
     * @param executor the thread pool for incoming requests
     * @param handler  the connection handler to use
     * @param scheme   the connection scheme this listener handles
     */
    public Listener(final ServerSocket socket,
                    final ExecutorService executor,
                    final ConnectionHandler handler,
                    final String scheme) {
        this.socket = socket;
        this.executor = executor;
        this.handler = handler;
        this.scheme = scheme;
    }

    /**
     * Run the listener.
     */
    @Override
    public void run() {
        logger.info("Now listening on port {}.", this.socket.getLocalPort());
        for (;;) {
            try (Socket client = this.socket.accept()) {
                client.setSoTimeout(4444);

                final InetAddress address = client.getInetAddress();
                final int port = client.getPort();

                this.executor.submit(() -> {
                    try (InputStream in = client.getInputStream();
                         OutputStream out = client.getOutputStream()) {
                        this.handler.handle(address, port, in, out, this.scheme);
                    } catch (final IOException ex) {
                        logger.error("I/O exception while accepting a client: ", ex);
                    }
                });
            } catch (final IOException e) {
                logger.error("I/O exception while accepting a client: ", e);
            }
        }
    }
}
