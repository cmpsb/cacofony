package net.wukl.cacofony.http2;

import net.wukl.cacofony.http2.frame.FrameReader;
import net.wukl.cacofony.http2.frame.FrameWriter;
import net.wukl.cacofony.server.Connection;
import net.wukl.cacofony.server.ServerSettings;
import net.wukl.cacofony.server.protocol.ProtocolFactory;

/**
 * A factory creating HTTP/2 protocol instances.
 */
public class Http2ProtocolFactory implements ProtocolFactory<Http2Protocol> {
    /**
     * The frame reader used by protocol instances.
     */
    private final FrameReader frameReader;

    /**
     * The frame writer used by protocol instances.
     */
    private final FrameWriter frameWriter;

    /**
     * The server settings used by protocol instances.
     */
    private final ServerSettings serverSettings;

    /**
     * Creates a new HTTP/2 protocol factory.
     *
     * @param frameReader the frame reader used by protocol instances
     * @param frameWriter the frame writer used by protocol instances
     * @param serverSettings the server settings used by protocol instances
     */
    public Http2ProtocolFactory(
            final FrameReader frameReader, final FrameWriter frameWriter,
            final ServerSettings serverSettings
    ) {
        this.frameReader = frameReader;
        this.frameWriter = frameWriter;
        this.serverSettings = serverSettings;
    }

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
        return new Http2Protocol(this.frameReader, this.frameWriter, this.serverSettings, conn);
    }
}
