package net.wukl.cacofony.http2.frame;

import net.wukl.cacofony.http2.Http2ConnectionError;

/**
 * An error indicating an erroneous frame size.
 */
public class Http2FrameSizeError extends Http2ConnectionError {
    /**
     * Creates a new http2 frame size exception.
     */
    public Http2FrameSizeError() {
        super();
    }

    /**
     * Creates a new http2 frame size exception.
     *
     * @param message the detail message explaining what caused the exception
     */
    public Http2FrameSizeError(final String message) {
        super(message);
    }

    /**
     * Creates a new http2 frame size exception.
     *
     * @param cause the exception that caused this exception
     */
    public Http2FrameSizeError(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new http2 frame size exception.
     *
     * @param message the detail message explaining what caused the exception
     * @param cause   the exception that caused this exception
     */
    public Http2FrameSizeError(final String message, final Throwable cause) {
        super(message, cause);
    }
}
