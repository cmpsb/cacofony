package net.wukl.cacofony.server.protocol;

import net.wukl.cacofony.server.Connection;

/**
 * A factory creating HTTP/2 protocol instances.
 */
public class Http2ProtocolFactory implements ProtocolFactory<Http2Protocol> {
    /**
     * Builds an instance of the protocol.
     * <p>
     * The protocol should be ready to handle incoming connections.
     *
     * @param conn the connection to build the protocol for
     *
     * @return the protocol instance
     */
    @Override
    public Http2Protocol build(final Connection conn) {
        return new Http2Protocol(conn);
    }
}
