package net.wukl.cacofony.server;

import java.io.IOException;

/**
 * A factory for listeners.
 *
 * @author Luc Everse
 */
public interface ListenerFactory {
    /**
     * Builds a listener listening on a port.
     *
     * @param port the port
     *
     * @return the listener
     *
     * @throws IOException if an I/O error occurs
     */
    Listener build(Port port) throws IOException;
}
