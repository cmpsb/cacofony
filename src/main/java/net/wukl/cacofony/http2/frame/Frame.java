package net.wukl.cacofony.http2.frame;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

/**
 * A HTTP/2 frame.
 */
public interface Frame {
    /**
     * Returns the number of bytes in the frame's payload.
     *
     * @return the length of the payload
     */
    int getLength();

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
     * Translates the frame payload into a set of bytes for transmission and writes them to an
     * output stream.
     *
     * @param out the stream to write the bytes to
     *
     * @throws IOException if an I/O error occurs
     */
    void writePayload(OutputStream out) throws IOException;
}
