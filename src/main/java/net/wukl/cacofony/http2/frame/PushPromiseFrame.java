package net.wukl.cacofony.http2.frame;

import java.util.Collections;
import java.util.Set;

/**
 * A PUSH_PROMISE frame.
 *
 * This frame announces the streams the server intends to initiate.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7540#section-6.6">RFC 7540 Section 6.6</a>
 */
public class PushPromiseFrame implements Frame {
    /**
     * The identifier of the stream this push is announced on.
     */
    private final int streamId;

    /**
     * If {@code true}, this is the last frame containing a header block fragment, if {@code false},
     * {@link ContinuationFrame}s will follow.
     */
    private final boolean last;

    /**
     * The identifier of the stream this promise is announcing.
     */
    private final int promisedStreamId;

    /**
     * The header byte fragment.
     */
    private final byte[] fragment;

    /**
     * Creates a new PUSH_PROMISE frame.
     *
     * @param streamId the identifier of the stream this promise is announced on
     * @param last whether this is the last frame containing a header block fragment or not
     * @param promisedStreamId the identifier of the stream this promise is announcing
     * @param fragment the header byte fragment
     */
    public PushPromiseFrame(
        final int streamId, final boolean last, final int promisedStreamId, final byte[] fragment
    ) {
        this.streamId = streamId;
        this.last = last;
        this.promisedStreamId = promisedStreamId;
        this.fragment = fragment;
    }

    /**
     * Returns the number of bytes in the frame's payload.
     *
     * @return the length of the payload
     */
    @Override
    public int getPayloadLength() {
        return Integer.BYTES + this.fragment.length;
    }

    /**
     * Returns the type of the frame.
     *
     * @return the frame type
     */
    @Override
    public FrameType getType() {
        return FrameType.PUSH_PROMISE;
    }

    /**
     * Returns the flags applied to the frame.
     *
     * @return the flags
     */
    @Override
    public Set<FrameFlag> getFlags() {
        if (this.last) {
            return Collections.singleton(FrameFlag.END_HEADERS);
        }

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
     * Returns the identifier of the stream this promise is announcing.
     *
     * @return the identifier of the promised stream
     */
    public int getPromisedStreamId() {
        return this.promisedStreamId;
    }

    /**
     * Returns the header block fragment.
     *
     * @return the fragment
     */
    public byte[] getFragment() {
        return this.fragment;
    }

    /**
     * Checks whether the frame is the last frame containing a header block for this promise.
     *
     * @return {@code true} if this is the last frame, {@code false} otherwise
     */
    public boolean isLast() {
        return this.last;
    }
}
