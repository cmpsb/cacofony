package net.wukl.cacofony.http2.frame;

import net.wukl.cacofony.http2.ErrorCode;

import java.util.Collections;
import java.util.Set;

/**
 * A RST_STREAM frame.
 *
 * This frame allows for the immediate termination of a stream.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7540#section-6.4">RFC 7540 Section 6.4</a>
 */
public class RstStreamFrame implements Frame {
    /**
     * The identifier of the stream that's being terminated.
     */
    private final int streamId;

    /**
     * The error code indicating the reason the stream was terminated.
     */
    private final ErrorCode errorCode;

    /**
     * Creates a new RST_STREAM frame.
     *
     * @param streamId the identifier of the stream that's being terminated
     * @param errorCode the error code indating the reason the stream is being terminated
     */
    public RstStreamFrame(final int streamId, final ErrorCode errorCode) {
        this.streamId = streamId;
        this.errorCode = errorCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPayloadLength() {
        return Integer.SIZE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FrameType getType() {
        return FrameType.RST_STREAM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<FrameFlag> getFlags() {
        return Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStreamId() {
        return this.streamId;
    }

    /**
     * Returns the error code indicating the reason the stream was terminated.
     *
     * @return the error code
     */
    public ErrorCode getErrorCode() {
        return this.errorCode;
    }
}
