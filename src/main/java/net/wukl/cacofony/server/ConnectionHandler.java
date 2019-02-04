package net.wukl.cacofony.server;

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
     * Handles an active connection.
     *
     * @param conn the connection to handle
     * @param iprotocol the initial protocol for the connection
     */
    public void handle(final Connection conn, final Protocol iprotocol) {
        try {
            logger.debug("Remote {} connected.", conn.getAddress());

            var protocol = iprotocol;
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
