package net.wukl.cacofony.server.protocol;

import net.wukl.cacofony.http.exception.NotImplementedException;
import net.wukl.cacofony.server.Connection;

/**
 * The HTTP/2 protocol.
 */
public class Http2Protocol implements Protocol {
    /**
     * The connection with the client.
     */
    private final Connection conn;

    /**
     * Creates a new HTTP/2 protocol instance.
     *
     * @param conn the connection with the client
     */
    public Http2Protocol(final Connection conn) {
        this.conn = conn;
    }

    /**
     * Returns the name of the protocol.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return "http/2";
    }

    /**
     * Handles an active connection.
     * <p>
     * The protocol should stay active until the connection can be closed.
     *
     * @return the next protocol to use for the connection
     *
     * @throws Throwable if any error occurs
     */
    @Override
    public Protocol handle() throws Throwable {
        throw new NotImplementedException("HTTP/2 is not fully implemented yet");
    }
}
