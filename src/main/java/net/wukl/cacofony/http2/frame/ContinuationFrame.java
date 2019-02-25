package net.wukl.cacofony.http2.frame;

import java.util.Collections;
import java.util.Set;

/**
 * A CONTINUATION frame.
 *
 * This frame is used to continue a sequence of header block fragments.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7540#section-6.10">RFC 7540 Section 6.10</a>
 */
public class ContinuationFrame implements Frame {
    /**
     * The identifier of the stream the header block fragment is for.
     */
    private final int streamId;

    /**
     * A flag indicating whether this frame ends the header block.
     */
    private final boolean last;

    /**
     * The header block fragment of the frame.
     */
    private final byte[] bytes;


    /**
     * Creates a new CONTINUATION frame.
     *
     * @param streamId the identifier of the stream the header block fragment is for
     * @param last {@code true} if this frame ends the header block
     * @param bytes the header block fragment of the frame
     */
    public ContinuationFrame(final int streamId, final boolean last, final byte[] bytes) {
        this.streamId = streamId;
        this.last = last;
        this.bytes = bytes;
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
        return FrameType.CONTINUATION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<FrameFlag> getFlags() {
        if (this.last) {
            return Collections.singleton(FrameFlag.END_HEADERS);
        }

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
     * Checks whether the frame ends the header block or more frames could follow.
     *
     * @return {@code true} if this is the last header block, {@code false} otherwise
     */
    public boolean isLast() {
        return this.last;
    }

    /**
     * Returns the header block fragment contained within this continuation.
     *
     * @return the header block fragment
     */
    public byte[] getBytes() {
        return this.bytes;
    }
}
