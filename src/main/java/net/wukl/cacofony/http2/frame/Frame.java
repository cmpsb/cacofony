package net.wukl.cacofony.http2.frame;

import java.util.Set;

/**
 * A HTTP/2 frame.
 */
public interface Frame {
    /**
     * Returns the type of the frame.
     *
     * @return the frame type
     */
    FrameType getType();

    /**
     * Returns the flags applied to the frame.
     *
     * @return the flags
     */
    Set<FrameFlag> getFlags();

    /**
     * Returns the identifier of the stream the frame belongs to.
     *
     * @return the stream identifier
     */
    int getStreamId();

    /**
     * Translates the frame payload into a set of bytes for transmission.
     *
     * @return the bytes
     */
    byte[] payloadToBytes();
}
