package net.wukl.cacofony.http2.frame;

import net.wukl.cacofony.http2.Http2ProtocolError;

import java.util.Collections;
import java.util.Set;

/**
 * A WINDOW_UPDATE frame.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7540#section-5.2">
 *     RFC 7540 Section 5.2 (Flow Control)</a>
 * @see <a href="https://tools.ietf.org/html/rfc7540#section-6.6">
 *     RFC 7540 Section 6.6 (WINDOW_UPDATE)</a>
 */
public class WindowUpdateFrame implements Frame {
    /**
     * The identifier of the stream the update applies to.
     */
    private final int streamId;

    /**
     * The increment in window size.
     */
    private final long increment;

    /**
     * Creates a new WINDOW_UPDATE frame.
     *
     * @param streamId the identifier of the stream the update applies to
     * @param increment the increment in window size, in bytes
     *
     * @throws Http2ProtocolError if the increment is outside the acceptable range of [1, 2^32-1]
     */
    public WindowUpdateFrame(final int streamId, final long increment) {
        this.streamId = streamId;
        this.increment = increment;

        if (increment < 1) {
            throw new Http2ProtocolError(
                    "Window size increment (" + increment + ") is not within [1, 2^31-1]"
            );
        }
    }

    /**
     * Returns the number of bytes in the frame's payload.
     *
     * @return the length of the payload
     */
    @Override
    public int getPayloadLength() {
        return 4;
    }

    /**
     * Returns the type of the frame.
     *
     * @return the frame type
     */
    @Override
    public FrameType getType() {
        return FrameType.WINDOW_UPDATE;
    }

    /**
     * Returns the flags applied to the frame.
     *
     * @return the flags
     */
    @Override
    public Set<FrameFlag> getFlags() {
        return Collections.emptySet();
    }

    /**
     * Returns the identifier of the stream the frame belongs to.
     *
     * @return the stream identifier
     */
    @Override
    public int getStreamId() {
        return this.streamId;
    }

    /**
     * Returns the increment in window size, in bytes.
     *
     * @return the increment
     */
    public long getIncrement() {
        return this.increment;
    }
}
