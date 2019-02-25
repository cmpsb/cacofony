package net.wukl.cacofony.http2.frame;

import java.util.Collections;
import java.util.Set;

/**
 * A PING frame.
 *
 * This frame provides a mechanism for measuring minimal round-trip time from the sender, as well
 * as determining whether an idle connection is still functional.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7540#section-6.7">RFC 7540 Section 6.7</a>
 */
public class PingFrame implements Frame {
    /**
     * The number of bytes the opaque payload may contain.
     */
    public static final int PAYLOAD_SIZE = 8;

    /**
     * Whether the frame is a ping acknowledgement or not.
     */
    private final boolean acknowledgement;

    /**
     * The opaque payload of the ping.
     */
    private final byte[] payload;

    /**
     * Creates a PING frame.
     *
     * @param acknowledgement iff {@code true}, this frame is an acknowledgement
     * @param payload the opaque payload bytes
     */
    public PingFrame(final boolean acknowledgement, final byte[] payload) {
        this.acknowledgement = acknowledgement;
        this.payload = payload;
    }

    /**
     * Returns the number of bytes in the frame's payload.
     *
     * @return the length of the payload
     */
    @Override
    public int getPayloadLength() {
        return PAYLOAD_SIZE;
    }

    /**
     * Returns the type of the frame.
     *
     * @return the frame type
     */
    @Override
    public FrameType getType() {
        return FrameType.PING;
    }

    /**
     * Returns the flags applied to the frame.
     *
     * @return the flags
     */
    @Override
    public Set<FrameFlag> getFlags() {
        if (this.acknowledgement) {
            return Collections.singleton(FrameFlag.ACK);
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
        return 0;
    }

    /**
     * Returns the opaque payload of the frame.
     *
     * @return the payload
     */
    public byte[] getPayload() {
        return this.payload;
    }
}
