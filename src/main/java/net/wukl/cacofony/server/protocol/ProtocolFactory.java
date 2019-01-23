package net.wukl.cacofony.server.protocol;

import net.wukl.cacofony.server.Connection;

/**
 * A factory for specific protocols.
 *
 * @param <P> the type of protocol this factory builds
 */
public interface ProtocolFactory<P extends Protocol> {
    /**
     * Builds an instance of the protocol.
     *
     * The protocol should be ready to handle incoming connections.
     *
     * @param conn the connection to build the protocol for
     *
     * @return the protocol instance
     */
    P build(Connection conn);
}
