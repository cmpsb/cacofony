package net.wukl.cacofony.http2;

import net.wukl.cacofony.http.request.Header;
import net.wukl.cacofony.http2.frame.ContinuationFrame;
import net.wukl.cacofony.http2.frame.DataFrame;
import net.wukl.cacofony.http2.frame.Frame;
import net.wukl.cacofony.http2.frame.FrameFlag;
import net.wukl.cacofony.http2.frame.FrameReader;
import net.wukl.cacofony.http2.frame.FrameType;
import net.wukl.cacofony.http2.frame.FrameWriter;
import net.wukl.cacofony.http2.frame.GoAwayFrame;
import net.wukl.cacofony.http2.frame.HeadersFrame;
import net.wukl.cacofony.http2.frame.PingFrame;
import net.wukl.cacofony.http2.frame.PushPromiseFrame;
import net.wukl.cacofony.http2.frame.RstStreamFrame;
import net.wukl.cacofony.http2.frame.SettingsFrame;
import net.wukl.cacofony.http2.frame.WindowUpdateFrame;
import net.wukl.cacofony.http2.hpack.Hpack;
import net.wukl.cacofony.http2.hpack.huffman.Huffman;
import net.wukl.cacofony.http2.settings.Setting;
import net.wukl.cacofony.http2.settings.SettingIdentifier;
import net.wukl.cacofony.http2.stream.Stream;
import net.wukl.cacofony.server.Connection;
import net.wukl.cacofony.server.ServerSettings;
import net.wukl.cacofony.server.protocol.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The HTTP/2 protocol.
 */
public class Http2Protocol implements Protocol {
    private static final Logger logger = LoggerFactory.getLogger(Http2Protocol.class);

    /**
     * The maximum amount of bytes to send in a single HEADERS frame.
     */
    private static final int MAX_HEADERS_FRAME_BYTES = 4096 - 9;

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
     * The initial maximum size of frames sent by the server.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7540#section-6.5.2">RFC 7540 Section 6.5.2</a>
     */
    private static final long INITIAL_MAX_FRAME_SIZE = 4096;

    /**
     * The initial window size for connections and streams.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7540#section-5.2.1">RFC 7540 Section 5.2.1</a>
     */
    private static final int INITIAL_WINDOW_SIZE = 65535;

    /**
     * The initial value of the initial stream window size.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7540#section-6.5.2">RFC 7540 Section 6.5.2</a>
     */
    private static final long DEFAULT_INITIAL_WINDOW_SIZE = 65535;

    /**
     * The HPACK codec to use.
     */
    private final Hpack hpack = new Hpack(new Huffman());

    /**
     * The HPACK lock for concurrent encoding.
     */
    private final Object hpackLock = new Object();

    /**
     * The frame writer to use.
     */
    private final FrameReader frameReader;

    /**
     * The frame writer to use.
     */
    private final FrameWriter frameWriter;

    /**
     * The request handler.
     */
    private final Http2RequestHandler requestHandler;

    /**
     * The executor service to run threads in.
     */
    private final ExecutorService executor;

    /**
     * Global server settings.
     */
    private final ServerSettings serverSettings;

    /**
     * The connection with the client.
     */
    private final Connection conn;

    /**
     * The streams active on the connection.
     */
    private final Map<Integer, Stream> streams = new HashMap<>();

    /**
     * The maximum size of a frame the client is willing to accept.
     */
    private long maxFrameSize = INITIAL_MAX_FRAME_SIZE;

    /**
     * The window size of newly created streams.
     */
    private long initialRemoteWindowSize = DEFAULT_INITIAL_WINDOW_SIZE;

    /**
     * The identifier of the last stream the server successfully processed.
     */
    private AtomicInteger lastHandledStream = new AtomicInteger(0);

    /**
     * The flow control window applying to the full connection.
     */
    private final Window globalWindow = new Window(INITIAL_WINDOW_SIZE, INITIAL_WINDOW_SIZE);

    /**
     * The queue of outbound frames.
     */
    private final BlockingDeque<Frame> outboundQueue = new LinkedBlockingDeque<>(4096);

    /**
     * Specialized frame handlers.
     */
    private final FrameHandler[] frameHandlers = new FrameHandler[256];

    /**
     * Whether the protocol is active or not.
     */
    private volatile boolean running = true;

    /**
     * Creates a new HTTP/2 protocol instance.
     * @param frameReader the frame reader to use
     * @param frameWriter the frame writer to use
     * @param requestHandler the request handler to use
     * @param executor the executor service to run threads in
     * @param serverSettings global server settings
     * @param conn the connection with the client
     */
    public Http2Protocol(
            final FrameReader frameReader,
            final FrameWriter frameWriter,
            final Http2RequestHandler requestHandler,
            final ExecutorService executor,
            final ServerSettings serverSettings,
            final Connection conn
    ) {
        this.frameReader = frameReader;
        this.frameWriter = frameWriter;
        this.requestHandler = requestHandler;
        this.executor = executor;
        this.serverSettings = serverSettings;
        this.conn = conn;

        for (int i = 0; i < (1 << Byte.SIZE); ++i) {
            final var index = i;
            this.frameHandlers[i] = f -> {
                logger.warn("Unrecognized frame type {} ({})", f.getType(), index);
            };
        }

        this.addHandler(FrameType.SETTINGS, this::handleSettings);
        this.addHandler(FrameType.WINDOW_UPDATE, this::handleWindowUpdate);
        this.addHandler(FrameType.PRIORITY, this::handlePriority);
        this.addHandler(FrameType.HEADERS, this::handleHeaders);
        this.addHandler(FrameType.DATA, this::handleData);
        this.addHandler(FrameType.CONTINUATION, this::handleContinuation);
        this.addHandler(FrameType.GOAWAY, this::handleGoAway);
        this.addHandler(FrameType.RST_STREAM, this::handleRstStream);
        this.addHandler(FrameType.PING, this::handlePing);
        this.addHandler(FrameType.PUSH_PROMISE, this::handlePushPromise);
    }

    /**
     * Installs a specialized frame handler in the handler table.
     *
     * @param type the frame type the handler is specialized for
     * @param handler the handler
     */
    private void addHandler(final FrameType type, final FrameHandler handler) {
        this.frameHandlers[type.getValue()] = handler;
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
        for (final var frame : extraFrames) {
            this.handleFrame(frame);
        }

        final var writerFuture = this.executor.submit(() -> {
            while (this.running) {
                try {
                    this.writeFrame(this.outboundQueue.take());
                } catch (final InterruptedException ex) {
                    // Ignore, check the running state again.
                } catch (final IOException ex) {
                    logger.error("I/O exception while writing a frame:", ex);
                    this.trap();
                } catch (final Throwable t) {
                    this.trap(t);
                }
            }
        });

        final var in = this.conn.getIn();
        while (this.running) {
            try {
                this.handleFrame(this.frameReader.read(in));
            } catch (final EOFException ex) {
                this.running = false;
            } catch (final Throwable t) {
                this.trap(t);
            }
        }

        writerFuture.cancel(true);
        return null;
    }

    /**
     * Handles an incoming frame.
     *
     * @param frame the frame to handle
     *
     * @throws Throwable if any error occurs
     */
    private void handleFrame(final Frame frame) throws Throwable {
        this.frameHandlers[frame.getType().getValue()].handle(frame);
    }

    /**
     * Processes a DATA frame.
     *
     * @param frame the frame
     *
     * @throws IOException if the payload cannot be forwarded to the running request
     *
     * @throws Throwable if any error occurs
     */
    private void handleData(final Frame frame) throws Throwable {
        assert frame instanceof DataFrame : "Non-DATA frame passed to handleData";

        final var streamId = frame.getStreamId();
        final var stream = this.streams.get(streamId);
        if (stream == null) {
            throw new Http2ProtocolError("Unknown or closed stream identifier " + streamId);
        }

        final var out = stream.getRequestPipe().getOut();
        out.write(((DataFrame) frame).getBytes());

        if (frame.getFlags().contains(FrameFlag.END_STREAM)) {
            out.close();
            this.streams.remove(streamId);
        }

        this.enqueueOutbound(
                new WindowUpdateFrame(0, frame.getPayloadLength()),
                new WindowUpdateFrame(streamId, frame.getPayloadLength())
        );
    }

    /**
     * Processes a HEADERS frame.
     *
     * @param frame the frame
     */
    private void handleHeaders(final Frame frame) {
        assert frame instanceof HeadersFrame : "Non-HEADERS frame passed to handleHeaders";
        final var headersFrame = (HeadersFrame) frame;

        final var streamId = frame.getStreamId();
        if (this.streams.containsKey(streamId)) {
            throw new Http2ProtocolError("Stream identifier " + streamId + " is already in use");
        }

        final var stream = new Stream(
                streamId, (int) this.initialRemoteWindowSize, INITIAL_WINDOW_SIZE
        );
        stream.addHeaderBytes(headersFrame.getHeaderBlock());
        this.streams.put(streamId, stream);

        final var priority = headersFrame.getPriorityFrame();
        if (priority != null) {
            this.handlePriority(priority);
        }

        if (frame.getFlags().contains(FrameFlag.END_HEADERS)) {
            this.handleRequest(stream);
        }
    }

    /**
     * Handles a PRIORITY frame.
     *
     * @param frame the frame
     */
    private void handlePriority(final Frame frame) {
        logger.warn("Ignoring priority frame for stream {}", frame.getStreamId());
    }

    /**
     * Handles an RST_STREAM frame.
     *
     * @param frame the frame
     *
     * @throws IOException if an I/O error occurs
     */
    private void handleRstStream(final Frame frame) throws IOException {
        assert frame instanceof RstStreamFrame : "Non-RST_STREAM frame passed to handleRstStream";

        final var id = frame.getStreamId();
        final var rst = (RstStreamFrame) frame;
        logger.debug("Client closed stream {}: {}", id, rst.getErrorCode());

        final var stream = this.streams.get(id);
        this.streams.remove(id);

        stream.close();
    }

    /**
     * Processes a SETTINGS frame.
     *
     * @param frame the frame
     */
    private void handleSettings(final Frame frame) {
        assert frame instanceof SettingsFrame : "Non-SETTINGS frame passed to handleSettings";

        for (final var setting : ((SettingsFrame) frame).getSettings()) {
            switch (setting.getIdentifier()) {
                case HEADER_TABLE_SIZE:
                    this.hpack.updateMaximumEncodingSize((int) setting.getValue(), true);
                    break;
                case INITIAL_WINDOW_SIZE:
                    this.initialRemoteWindowSize = setting.getValue();
                    break;
                case MAX_FRAME_SIZE:
                    this.maxFrameSize = setting.getValue();
                    break;
                default:
                    logger.warn("Ignoring setting {} with value {}",
                        setting.getIdentifier(), setting.getValue()
                    );
                    break;
            }
        }
    }

    /**
     * Processes a PUSH_PROMISE frame.
     *
     * Since the client should not send such frames, this will always error.
     *
     * @param frame frame
     */
    private void handlePushPromise(final Frame frame) {
        assert frame instanceof PushPromiseFrame
                : "Non-PUSH_PROMISE frame passed to handlePushPromise";

        throw new Http2ProtocolError("Received unexpected PUSH_PROMISE");
    }

    /**
     * Processes a PING frame.
     *
     * @param frame the frame
     *
     * @throws InterruptedException if the outbound queue was full and the thread was
     *      interrupted while waiting for space in the queue
     */
    private void handlePing(final Frame frame) throws InterruptedException {
        assert frame instanceof PingFrame : "Non-PING frame passed to handlePing";

        if (frame.getFlags().contains(FrameFlag.ACK)) {
            logger.debug("Received PING acknowledgement");
            return;
        }

        // Does not send the response "as soon as possible", but there is no way to enqueue it
        // as close to the front as possible, but after any CONTINUATION frames.
        this.enqueueOutbound(new PingFrame(true, ((PingFrame) frame).getPayload()));
    }

    /**
     * Handles a GOAWAY frame.
     *
     * @param frame the frame
     *
     * @throws IOException if an I/O error occurs while closing the connection
     */
    private void handleGoAway(final Frame frame) throws IOException {
        assert frame instanceof GoAwayFrame : "Non-GOAWAY frame passed to handleGoAway";

        final var goaway = (GoAwayFrame) frame;
        final var errorCode = goaway.getErrorCode();
        if (errorCode != ErrorCode.NO_ERROR) {
            logger.error("Client closed connection with an error: {}", errorCode);

            final var debug = goaway.getDebugData();
            if (debug.length > 0) {
                logger.debug("Debug data: {}", new String(debug, StandardCharsets.UTF_8));
            }
        }

        this.conn.getIn().close();
    }

    /**
     * Processes an incoming WINDOW_UPDATE frame.
     *
     * @param frame the frame
     */
    private void handleWindowUpdate(final Frame frame) {
        assert frame instanceof WindowUpdateFrame
                : "Non-WINDOW_UPDATE frame passed to handleWindowUpdate";

        final var update = (WindowUpdateFrame) frame;

        if (frame.getStreamId() == 0) {
            this.globalWindow.topOffRemote((int) update.getIncrement());
        } else {
            final var stream = this.streams.get(frame.getStreamId());
            if (stream != null) {
                stream.getWindow().topOffRemote((int) update.getIncrement());
            }
        }
    }

    /**
     * Processes an incoming CONTINUATION frame.
     *
     * @param frame the frame
     */
    private void handleContinuation(final Frame frame) {
        assert frame instanceof ContinuationFrame
            : "Non-CONTINUATION frame passed to handleContinuation";

        final var stream = this.streams.get(frame.getStreamId());
        if (stream == null) {
            throw new Http2ProtocolError("No stream with identifier " + frame.getStreamId());
        }

        stream.addHeaderBytes(((ContinuationFrame) frame).getBytes());

        if (frame.getFlags().contains(FrameFlag.END_HEADERS)) {
            this.handleRequest(stream);
        }
    }

    /**
     * Hands over a complete request to the request handler.
     *
     * @param stream the stream the request is on
     */
    private void handleRequest(final Stream stream) {
        final var headers = this.hpack.decompress(stream.getHeaderBytes());
        stream.setHeaders(headers);
        stream.associateFuture(this.requestHandler.handleRequest(this, this.conn, stream));
    }

    /**
     * Traps the protocol, terminating the connection.
     */
    public void trap() {
        try {
            this.running = false;
            this.conn.getIn().close();
        } catch (final IOException ex) {
            logger.warn("Exception while closing the connection to the client:", ex);
        }
    }

    /**
     * Traps the protocol with an unhandled exception, terminating the connection.
     *
     * @param t the throwable that caused the unrecoverable situation
     */
    public void trap(final Throwable t) {
        logger.error("Unhandled exception", t);
        this.trap();
    }

    /**
     * Enqueues frames for transmission.
     *
     * The frames are sent in the order as supplied, with no other frames in between.
     *
     * @param frames the frames to send
     *
     * @throws InterruptedException
     *      if the queue was full and the thread was interrupted while waiting
     */
    public void enqueueOutbound(final Frame... frames) throws InterruptedException {
        synchronized (this.outboundQueue) {
            for (final var frame : frames) {
                this.outboundQueue.put(frame);
            }
        }
    }

    /**
     * Enqueues frames for transmission.
     *
     * The frames are sent in the order as supplied, but possibly with other frames in between.
     *
     * @param frames the frames to send
     *
     * @throws InterruptedException
     *      if the queue was full and the thread was interrupted while waiting
     */
    public void enqueueOutboundLowPriority(final Frame... frames) throws InterruptedException {
        for (final var frame : frames) {
            synchronized (this.outboundQueue) {
                this.outboundQueue.put(frame);
            }
        }
    }

    /**
     * Sends the list of response headers to the client.
     *
     * Streams sending headers MUST use this method to prevent desynchronization between HPACK state
     * and the order in which frames are sent to the client.
     *
     * @param streamId the identifier of the stream the headers are for
     * @param headers the headers to send
     *
     * @throws InterruptedException
     *      if the outbound queue was full and the thread was interrupted while waiting
     */
    public void sendHeaders(
            final int streamId, final List<Header> headers
    ) throws InterruptedException {
        synchronized (this.hpackLock) {
            synchronized (this.outboundQueue) {
                final var bytes = this.hpack.compress(headers);
                final var numFrames = (bytes.length - 1) / MAX_HEADERS_FRAME_BYTES + 1;

                if (numFrames == 1) {
                    final var frame = new HeadersFrame(
                            Set.of(FrameFlag.END_HEADERS),
                            streamId,
                            bytes,
                            null
                    );

                    this.enqueueOutbound(frame);
                    return;
                }

                final var headerFrames = new Frame[numFrames];
                headerFrames[0] = new HeadersFrame(
                        Collections.emptySet(),
                        streamId,
                        Arrays.copyOf(bytes, MAX_HEADERS_FRAME_BYTES),
                        null
                );

                int bytesProcessed = MAX_HEADERS_FRAME_BYTES;
                for (int i = 1; i < headerFrames.length; ++i) {
                    int bytesToSend = Math.min(
                            bytes.length - bytesProcessed, MAX_HEADERS_FRAME_BYTES
                    );

                    final var payload = Arrays.copyOfRange(
                            bytes, bytesProcessed, bytesProcessed + bytesToSend
                    );
                    headerFrames[i] = new ContinuationFrame(
                            streamId, i + 1 == numFrames, payload
                    );
                    bytesProcessed += bytesToSend;
                }

                this.enqueueOutbound(headerFrames);
            }
        }
    }

    /**
     * Writes a frame to the output stream of the connection.
     *
     * @param frame the frame to write
     *
     * @throws IOException if an I/O error occurs
     */
    private void writeFrame(final Frame frame) throws IOException {
        this.frameWriter.write(frame, this.conn.getOut());
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

    /**
     * Returns the maximum size of the payload for any frame.
     *
     * @return the maximum payload size
     */
    public int getMaxFrameSize() {
        return (int) this.maxFrameSize;
    }

    /**
     * Updates the identifier of the last successfully completed stream.
     *
     * @param streamId the identifier
     */
    public void updateLastHandledStream(final int streamId) {
        this.lastHandledStream.updateAndGet(o -> Math.max(o, streamId));
    }

    /**
     * A function handling a frame.
     */
    @FunctionalInterface
    private interface FrameHandler {
        /**
         * Handles a specific frame.
         *
         * @param frame the frame to handle
         *
         * @throws Throwable if any error occurs
         */
        void handle(Frame frame) throws Throwable;
    }
}
