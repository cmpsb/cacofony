package net.wukl.cacofony.http2;

/**
 * An HTTP/2 connection error.
 *
 * Most exceptions raised by the HTTP/2 stack are subclasses of this class.
 */
public class Http2ConnectionError extends RuntimeException {
    /**
     * Creates a new connection exception.
     */
    public Http2ConnectionError() {
        super();
    }

    /**
     * Creates a new connection exception.
     *
     * @param message the detail message explaining what caused the exception
     */
    public Http2ConnectionError(final String message) {
        super(message);
    }

    /**
     * Creates a new connection exception.
     *
     * @param cause the exception that caused this exception
     */
    public Http2ConnectionError(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new connection exception.
     *
     * @param message the detail message explaining what caused the exception
     * @param cause   the exception that caused this exception
     */
    public Http2ConnectionError(final String message, final Throwable cause) {
        super(message, cause);
    }
}
