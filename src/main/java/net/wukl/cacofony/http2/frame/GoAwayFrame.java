package net.wukl.cacofony.http2.frame;

import net.wukl.cacofony.http2.ErrorCode;

import java.util.Collections;
import java.util.Set;

/**
 * A GOAWAY frame.
 *
 * Initiates the shutdown of a connection and signals serious error conditions.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7540#section-6.8">RFC 7540 Section 6.8</a>
 */
public class GoAwayFrame implements Frame {
    /**
     * The identifier of the last stream the server successfully processed.
     */
    private final int lastStreamId;

    /**
     * The error code describing why the connection is being terminated.
     */
    private final ErrorCode errorCode;

    /**
     * Any (opaque) debug data.
     */
    private final byte[] debugData;

    /**
     * Creates a new GOAWAY frame.
     *
     * @param lastStreamId the identifier of the last stream the server successfully processed
     * @param errorCode the error code describing why the connection is being terminated
     * @param debugData any (opaque) debug data
     */
    public GoAwayFrame(final int lastStreamId, final ErrorCode errorCode, final byte[] debugData) {
        this.lastStreamId = lastStreamId;
        this.errorCode = errorCode;
        this.debugData = debugData;
    }

    /**
     * Returns the number of bytes in the frame's payload.
     *
     * @return the length of the payload
     */
    @Override
    public int getPayloadLength() {
        return 2 * Integer.BYTES + this.debugData.length;
    }

    /**
     * Returns the type of the frame.
     *
     * @return the frame type
     */
    @Override
    public FrameType getType() {
        return FrameType.GOAWAY;
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
        return 0;
    }

    /**
     * Returns the identifier of the last stream the server successfully processed.
     *
     * @return the identifier of the last stream
     */
    public int getLastStreamId() {
        return this.lastStreamId;
    }

    /**
     * Returns the error code describing why the connection is being terminated.
     *
     * @return the error code
     */
    public ErrorCode getErrorCode() {
        return this.errorCode;
    }

    /**
     * Returns the (opaque) debug data.
     *
     * @return the data
     */
    public byte[] getDebugData() {
        return this.debugData;
    }
}
