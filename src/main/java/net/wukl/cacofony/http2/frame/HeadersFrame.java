package net.wukl.cacofony.http2.frame;

import java.util.HashSet;
import java.util.Set;

/**
 * A HEADERS frame.
 *
 * This frame initiates a new stream and contains zero or more headers. Additional headers are
 * contained in CONTINUATION frames.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7540#section-6.2">RFC 7540 Section 6.2</a>
 */
public class HeadersFrame implements Frame {
    /**
     * Flags set for the frame.
     */
    private final Set<FrameFlag> flags;

    /**
     * The identifier of the stream this block opens.
     */
    private final int streamId;

    /**
     * The raw header block compressed using HPACK.
     */
    private final byte[] headerBlock;

    /**
     * A PRIORITY frame representing the optional priority part of the frame.
     */
    private final PriorityFrame priorityFrame;

    /**
     * Creates a new HEADERS frame.
     *
     * @param flags any flags that were set for the frame
     * @param streamId the identifier of the stream the frame opens
     * @param headerBlock the actual header block
     * @param priorityFrame if non-{@code null}, priority information as a virtual frame
     */
    public HeadersFrame(
            final Set<FrameFlag> flags, final int streamId,
            final byte[] headerBlock, final PriorityFrame priorityFrame
    ) {
        this.flags = flags;
        this.streamId = streamId;
        this.headerBlock = headerBlock;
        this.priorityFrame = priorityFrame;
    }

    /**
     * Returns the frame's payload length.
     *
     * If the frame contains priority information, this length will include the five bytes consumed
     * by that. It does not include the padding bytes (either the length byte or NULs).
     *
     * @return the payload length
     */
    @Override
    public int getPayloadLength() {
        if (this.priorityFrame != null) {
            return this.headerBlock.length + 5;
        }

        return this.headerBlock.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FrameType getType() {
        return FrameType.HEADERS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<FrameFlag> getFlags() {
        final var trueFlags = new HashSet<>(this.flags);
        if (this.priorityFrame == null) {
            trueFlags.remove(FrameFlag.PRIORITY);
        } else {
            trueFlags.add(FrameFlag.PRIORITY);
        }

        return trueFlags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStreamId() {
        return this.streamId;
    }

    /**
     * Returns the (partial) header block within the frame.
     *
     * @return the header block
     */
    public byte[] getHeaderBlock() {
        return this.headerBlock;
    }

    /**
     * Returns the frame's priority information as a virtual PRIORITY frame.
     *
     * @return the virtual PRIORITY frame
     */
    public PriorityFrame getPriorityFrame() {
        return this.priorityFrame;
    }
}
