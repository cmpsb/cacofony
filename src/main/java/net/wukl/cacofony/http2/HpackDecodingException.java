package net.wukl.cacofony.http2;

/**
 * @author Luc Everse
 */
public class HpackDecodingException extends RuntimeException {
    /**
     * Creates a new hpack decoding exception.
     */
    public HpackDecodingException() {
        super();
    }

    /**
     * Creates a new hpack decoding exception.
     *
     * @param message the detail message explaining what caused the exception
     */
    public HpackDecodingException(final String message) {
        super(message);
    }

    /**
     * Creates a new hpack decoding exception.
     *
     * @param cause the exception that caused this exception
     */
    public HpackDecodingException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new hpack decoding exception.
     *
     * @param message the detail message explaining what caused the exception
     * @param cause   the exception that caused this exception
     */
    public HpackDecodingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
