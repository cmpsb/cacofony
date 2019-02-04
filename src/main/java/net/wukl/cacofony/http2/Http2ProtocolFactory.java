package net.wukl.cacofony.http2;

import net.wukl.cacofony.server.Connection;
import net.wukl.cacofony.server.protocol.ProtocolFactory;

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
