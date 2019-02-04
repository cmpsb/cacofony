package net.wukl.cacofony.http2.hpack;

/**
 * @author Luc Everse
 */
public class HpackEncodingException extends RuntimeException {
    /**
     * Creates a new encoding exception.
     */
    public HpackEncodingException() {
        super();
    }

    /**
     * Creates a new encoding exception.
     *
     * @param message the detail message explaining what caused the exception
     */
    public HpackEncodingException(final String message) {
        super(message);
    }

    /**
     * Creates a new encoding exception.
     *
     * @param cause the exception that caused this exception
     */
    public HpackEncodingException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new encoding exception.
     *
     * @param message the detail message explaining what caused the exception
     * @param cause   the exception that caused this exception
     */
    public HpackEncodingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
