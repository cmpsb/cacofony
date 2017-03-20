package net.cmpsb.cacofony.http;

import net.cmpsb.cacofony.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * A listener for incoming requests.
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
     * The router to run for each request.
     */
    private final Router router;

    /**
     * The request parser.
     */
    private final RequestParser parser;

    /**
     * Run the listener.
     */
    @Override
    public void run() {
        for (;;) {
            try {
                final Socket client = socket.accept();

                final BufferedReader in = new BufferedReader(
                        new InputStreamReader(client.getInputStream())
                );
            } catch (final IOException e) {
                logger.error("I/O exception while serving a client: ", e);
            }
        }
    }
}
