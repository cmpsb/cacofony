package net.cmpsb.cacofony.server;

import java.io.IOException;

/**
 * A factory for listeners.
 * The factory should automatically start the listener as well.
 *
 * @author Luc Everse
 */
public interface ListenerFactory {
    /**
     * Boots a listener listening on a port.
     *
     * @param port the port
     *
     * @throws IOException if an I/O error occurs
     */
    void boot(Port port) throws IOException;
}
