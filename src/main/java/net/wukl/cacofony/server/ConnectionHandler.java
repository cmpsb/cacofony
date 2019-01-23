package net.wukl.cacofony.server;

import net.wukl.cacofony.server.protocol.HttpProtocolFactory;
import net.wukl.cacofony.server.protocol.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A handler for active connections.
 *
 * @author Luc Everse
 */
public class ConnectionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionHandler.class);

    /**
     * The factory constructing HTTP protocol instances.
     */
    private final HttpProtocolFactory httpProtocolFactory;

    /**
     * Creates a new connection handler.
     *
     * @param httpProtocolFactory the HTTP protocol factory to use
     */
    public ConnectionHandler(final HttpProtocolFactory httpProtocolFactory) {
        this.httpProtocolFactory = httpProtocolFactory;
    }

    /**
     * Handles an active connection.
     *
     * @param conn the connection to handle
     */
    public void handle(final Connection conn) {
        try {
            logger.debug("Remote {} connected.", conn.getAddress());

            Protocol protocol = this.httpProtocolFactory.build(conn);
            while (protocol != null) {
                protocol = protocol.handle();
            }

            logger.debug("Remote {} disconnected.", conn.getAddress());
        } catch (final IOException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Connection reset by peer")) {
                logger.debug("Client closed connection.");
            } else {
                logger.error("I/O exception while serving a client: ", ex);
            }
        } catch (final Throwable ex) {
            logger.error("Fatal exception: ", ex);
            throw new RuntimeException(ex);
        }
    }
}
