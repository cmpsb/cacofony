package net.wukl.cacofony.http2.frame;

import java.util.Set;

/**
 * A DATA frame.
 *
 * This frame contains opaque connection data.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7540#section-6.1">RFC 7540 Section 6.1</a>
 */
public class DataFrame implements Frame {
    /**
     * The identifier of the stream the data is for.
     */
    private final int streamId;

    /**
     * Any flags set for the frame.
     */
    private final Set<FrameFlag> flags;

    /**
     * The payload of the frame.
     */
    private final byte[] bytes;

    /**
     * Creates a new DATA frame.
     *
     * @param streamId the identifier of the stream the data is for
     * @param flags any flags set for the frame
     * @param bytes the payload of the frame
     */
    public DataFrame(final int streamId, final Set<FrameFlag> flags, final byte[] bytes) {
        this.streamId = streamId;
        this.bytes = bytes;
        this.flags = flags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPayloadLength() {
        return this.bytes.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FrameType getType() {
        return FrameType.DATA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<FrameFlag> getFlags() {
        return this.flags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStreamId() {
        return this.streamId;
    }

    /**
     * The payload of the frame.
     *
     * @return the payload bytes
     */
    public byte[] getBytes() {
        return this.bytes;
    }
}
