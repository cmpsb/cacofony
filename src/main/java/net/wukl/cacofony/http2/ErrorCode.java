package net.wukl.cacofony.http2;

/**
 * An HTTP/2 error code.
 */
public enum ErrorCode {
    /**
     * Not an actual error.
     */
    NO_ERROR(0x0),

    /**
     * An unspecific protocol error.
     */
    PROTOCOL_ERROR(0x1),

    /**
     * An unexpected internal error.
     */
    INTERNAL_ERROR(0x2),

    /**
     * The peer violated the flow-control protocol.
     */
    FLOW_CONTROL_ERROR(0x3),

    /**
     * The peer did not respond to the settings in a timely manner.
     */
    SETTINGS_TIMEOUT(0x4),

    /**
     * The peer sent a frame for a half-closed stream.
     */
    STREAM_CLOSED(0x5),

    /**
     * The peer sent a frame with an invalid size.
     */
    FRAME_SIZE_ERROR(0x6),

    /**
     * The endpoint refuses to accept the new stream.
     */
    REFUSED_STREAM(0x7),

    /**
     * Cancels a stream.
     */
    CANCEL(0x8),

    /**
     * The endpoint is unable to maintain header compression.
     */
    COMPRESSION_ERROR(0x9),

    /**
     * The connection established through a CONNECT request was abnormally closed.
     */
    CONNECT_ERROR(0xA),

    /**
     * The peer is overloading the endpoint.
     */
    ENHANCE_YOUR_CALM(0xB),

    /**
     * The connection transport does not meet minimum security requirements.
     */
    INADEQUATE_SECURITY(0xC),

    /**
     * The endpoint requires HTTP/1.1.
     */
    HTTP_1_1_REQUIRED(0xD),

    /**
     * An error code not recognized by the server software.
     *
     * This is a non-standard error code (with a value exceeding 2^32 - 1),
     * <em>do not send this.</em>
     */
    UNRECOGNIZED_ERROR(Long.MAX_VALUE);

    /**
     * Looks up the error code instance associated with the given 32-bit numeric value.
     *
     * If the value is not recognized, {@link #UNRECOGNIZED_ERROR} is returned instead.
     *
     * @param code the code to look up
     *
     * @return the error code instance
     */
    public static ErrorCode getForCode(final long code) {
        if (code < 0 || code > HTTP_1_1_REQUIRED.code) {
            return UNRECOGNIZED_ERROR;
        }

        final var value = values()[(int) code];
        assert value.code == code;
        return value;
    }

    /**
     * The 32-bit numeric code representing the error.
     */
    private final long code;

    /**
     * Creates a new HTTP/2 error code.
     *
     * @param code the 32-bit numeric code representing the error
     */
    ErrorCode(final long code) {
        this.code = code;
    }

    /**
     * Returns the 32-bit numeric code representing the error.
     *
     * @return the code
     */
    public long getCode() {
        return this.code;
    }
}
