package net.wukl.cacofony.http2.frame;

/**
 * Possible flags for frames.
 *
 * Flag names are shared between some frames, but their positions may differ.
 * NEVER use the ordinal of the enum to determine the corresponding position; use
 * {@link FrameType#getFlagPosition(FrameFlag)} or {@link #getPosition} instead.
 */
public enum FrameFlag {
    /**
     * The current frame closes the stream.
     */
    END_STREAM,

    /**
     * The frame payload is padded with extra bytes.
     */
    PADDED,

    /**
     * The current frame ends the header block.
     */
    END_HEADERS,

    /**
     * The current frame contains additional stream priority information.
     */
    PRIORITY,

    /**
     * The current frame acknowledges the receipt of a previous frame.
     */
    ACK;

    /**
     * Returns the position of the flag if it were applied to the given frame type.
     *
     * If the flag is not valid for the given type, {@code null} is returned instead.
     *
     * @param type the frame type the flag is for
     *
     * @return the position or {@code null} if the flag is not valid for the given type
     */
    public Integer getPosition(final FrameType type) {
        return type.getFlagPosition(this);
    }
}
