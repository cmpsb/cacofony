package net.wukl.cacofony.http2;

/**
 * An exception indicating an HTTP/2 protocol error.
 */
public class Http2ProtocolError extends Http2ConnectionError {
    /**
     * Creates a new http2 protocol exception.
     */
    public Http2ProtocolError() {
        super();
    }

    /**
     * Creates a new http2 protocol exception.
     *
     * @param message the detail message explaining what caused the exception
     */
    public Http2ProtocolError(final String message) {
        super(message);
    }

    /**
     * Creates a new http2 protocol exception.
     *
     * @param cause the exception that caused this exception
     */
    public Http2ProtocolError(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new http2 protocol exception.
     *
     * @param message the detail message explaining what caused the exception
     * @param cause   the exception that caused this exception
     */
    public Http2ProtocolError(final String message, final Throwable cause) {
        super(message, cause);
    }
}
