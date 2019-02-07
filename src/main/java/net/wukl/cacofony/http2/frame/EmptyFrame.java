package net.wukl.cacofony.http2.frame;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

/**
 * A frame with no associated payload.
 *
 * The semantics of the frame differ between the users of the frame. Its uses can be:
 * - a frame yet to be interpreted by a reading process;
 * - a frame with a type that never contains a payload
 * - a frame with an invalid frame type that may or may not have had a payload
 */
public class EmptyFrame implements Frame {
    /**
     * The length of the payload.
     */
    private final int length;

    /**
     * The type of the frame.
     */
    private final FrameType type;

    /**
     * The flags of the frame.
     */
    private final Set<FrameFlag> flags;

    /**
     * The id of the stream the frame belongs to.
     */
    private final int streamId;

    /**
     * Creates a new empty frame.
     *
     * @param length the length of the (virtual) payload
     * @param type the type of the frame
     * @param flags the flags belonging to the frame
     * @param streamId the stream id of the stream the frame belongs to
     */
    public EmptyFrame(
            final int length, final FrameType type, final Set<FrameFlag> flags, final int streamId
    ) {
        this.length = length;
        this.type = type;
        this.flags = flags;
        this.streamId = streamId;
    }

    /**
     * Returns the number of bytes in the frame's payload.
     *
     * This returns the internally registered length of the payload. Even if this value is non-zero,
     * writing the payload to an output stream remains a no-op.
     *
     * @return the length of the payload
     */
    @Override
    public int getPayloadLength() {
        return this.length;
    }

    /**
     * Returns the type of the frame.
     *
     * @return the frame type
     */
    @Override
    public FrameType getType() {
        return this.type;
    }

    /**
     * Returns the flags applied to the frame.
     *
     * @return the flags
     */
    @Override
    public Set<FrameFlag> getFlags() {
        return this.flags;
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
     * Translates the frame payload into a set of bytes for transmission and writes them to an
     * output stream.
     *
     * Since this class guarantees no usable (thus practically empty) payload,
     * this returns immediately. This holds even if {@link #getPayloadLength()} returns non-zero.
     *
     * @param out the stream to write the bytes to
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void writePayload(final OutputStream out) throws IOException {
    }
}
