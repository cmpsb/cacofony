package net.wukl.cacofony.http2;

import net.wukl.cacofony.http.exception.NotImplementedException;
import net.wukl.cacofony.http2.frame.Frame;
import net.wukl.cacofony.http2.frame.FrameReader;
import net.wukl.cacofony.http2.frame.FrameType;
import net.wukl.cacofony.http2.frame.FrameWriter;
import net.wukl.cacofony.http2.frame.SettingsFrame;
import net.wukl.cacofony.http2.settings.Setting;
import net.wukl.cacofony.http2.settings.SettingIdentifier;
import net.wukl.cacofony.server.Connection;
import net.wukl.cacofony.server.ServerSettings;
import net.wukl.cacofony.server.protocol.Protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
     * The frame writer to use.
     */
    private final FrameReader frameReader;

    /**
     * The frame writer to use.
     */
    private final FrameWriter frameWriter;

    /**
     * Global server settings.
     */
    private final ServerSettings serverSettings;

    /**
     * The connection with the client.
     */
    private final Connection conn;


    /**
     * Creates a new HTTP/2 protocol instance.
     *  @param frameReader the frame reader to use
     * @param frameWriter the frame writer to use
     * @param serverSettings global server settings
     * @param conn the connection with the client
     */
    public Http2Protocol(
            final FrameReader frameReader, final FrameWriter frameWriter,
            final ServerSettings serverSettings, final Connection conn
    ) {
        this.frameReader = frameReader;
        this.frameWriter = frameWriter;
        this.serverSettings = serverSettings;
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
        final var extraFrames = this.handshake();
        throw new NotImplementedException(
                "HTTP/2 is not fully implemented yet, ignoring "
                        + extraFrames.size() + " extra frames"
        );
    }

    /**
     * Performs the initial HTTP/2 handshake.
     *
     * @return any extra non-SETTINGS frames received while performing the handshake
     *
     * @throws IOException if an I/O error occurs
     * @throws Http2ProtocolError if the handshake fails
     */
    private List<Frame> handshake() throws IOException {
        final var extraFrames = new ArrayList<Frame>();

        // Start by writing the server's settings.
        final var initSettings = new SettingsFrame(List.of(
                new Setting(
                        SettingIdentifier.MAX_CONCURRENT_STREAMS,
                        this.serverSettings.getMaxConcurrentStreams()
                )
        ));
        this.frameWriter.write(initSettings, this.conn.getOut());

        // Then wait for the client's preface and settings
        final var in = this.conn.getIn();
        final var clientPreface = in.readNBytes(CLIENT_PREFACE.length);
        if (!Arrays.equals(clientPreface, CLIENT_PREFACE)) {
            throw new Http2ProtocolError("The client did not send a proper connection prefix");
        }

        final var clientSettings = this.frameReader.read(in);
        if (clientSettings.getType() != FrameType.SETTINGS) {
            throw new Http2ProtocolError("First frame sent by client is not a settings frame "
                    + "(but " + clientSettings.getType().toString() + " instead)"
            );
        }
        extraFrames.add(clientSettings);

        // Finally, await the settings acknowledgement from the client
        while (true) {
            final var frame = this.frameReader.read(in);
            if (frame.getType() == FrameType.SETTINGS) {
                final var settingsFrame = (SettingsFrame) frame;
                if (settingsFrame.isAcknowledgement()) {
                    break;
                }
            }

            extraFrames.add(frame);
        }

        return extraFrames;
    }
}
