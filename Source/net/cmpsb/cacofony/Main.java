package net.cmpsb.cacofony;

import net.cmpsb.cacofony.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The default entry point.
 *
 * @author Luc Everse
 */
public final class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Do not instantiate.
     */
    private Main() {
        throw new AssertionError("Do not instantiate.");
    }

    /**
     * Start the server.
     *
     * @param args any command-line arguments
     */
    public static void main(final String[] args) {
        try {
            final Server server = new Server();
            server.addPort(8080);
            server.start();
        } catch (final Throwable t) {
            logger.error("Unable to start the server:", t);
        }
    }
}
