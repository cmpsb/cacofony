package net.wukl.cacofony.http2;

import net.wukl.cacofony.http.exception.NotImplementedException;
import net.wukl.cacofony.server.Connection;
import net.wukl.cacofony.server.protocol.Protocol;

import java.io.IOException;
import java.util.Arrays;

/**
 * The HTTP/2 protocol.
 */
public class Http2Protocol implements Protocol {
    /**
     * The byte sequence the client should send to establish an HTTP/2 connection.
     */
    private static final byte[] CLIENT_PREFACE = new byte[] {
            (byte) 0x50, (byte) 0x52, (byte) 0x49, (byte) 0x20, (byte) 0x2a, (byte) 0x20,
            (byte) 0x48, (byte) 0x54, (byte) 0x54, (byte) 0x50, (byte) 0x2f, (byte) 0x32,
            (byte) 0x2e, (byte) 0x30, (byte) 0x0d, (byte) 0x0a, (byte) 0x0d, (byte) 0x0a,
            (byte) 0x53, (byte) 0x4d, (byte) 0x0d, (byte) 0x0a, (byte) 0x0d, (byte) 0x0a,
    };

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
        this.handshake();
        throw new NotImplementedException("HTTP/2 is not fully implemented yet");
    }

    /**
     * Performs the initial HTTP/2 handshake.
     *
     * @throws IOException if an I/O error occurs
     * @throws Http2ProtocolError if the handshake fails
     */
    private void handshake() throws IOException {
        final var clientPreface = this.conn.getIn().readNBytes(CLIENT_PREFACE.length);
        if (!Arrays.equals(clientPreface, CLIENT_PREFACE)) {
            throw new Http2ProtocolError("The client did not send a proper connection prefix");
        }
    }
}
