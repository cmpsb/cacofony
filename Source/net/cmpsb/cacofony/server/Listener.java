package net.cmpsb.cacofony.server;

import net.cmpsb.cacofony.di.Inject;
import net.cmpsb.cacofony.di.MultiInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
     * The connection handler to use.
     */
    @Inject private ConnectionHandler handler;

    /**
     * Creates a new listener.
     *
     * @param socket   the socket to listen on
     * @param executor the thread pool for incoming requests
     */
    public Listener(@Inject("arg:socket")
                    final ServerSocket socket,
                    @Inject("name:resource/server.thread-pool")
                    final ExecutorService executor) {
        this.socket = socket;
        this.executor = executor;
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
                client.setSoTimeout(4444);

                this.executor.submit(() -> this.handler.handle(client));
            } catch (final IOException e) {
                logger.error("I/O exception while accepting a client: ", e);
            }
        }
    }
}
