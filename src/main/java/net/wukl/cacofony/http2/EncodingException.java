package net.wukl.cacofony.http2;

/**
 * @author Luc Everse
 */
public class EncodingException extends RuntimeException {
    /**
     * Creates a new encoding exception.
     */
    public EncodingException() {
        super();
    }

    /**
     * Creates a new encoding exception.
     *
     * @param message the detail message explaining what caused the exception
     */
    public EncodingException(final String message) {
        super(message);
    }

    /**
     * Creates a new encoding exception.
     *
     * @param cause the exception that caused this exception
     */
    public EncodingException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new encoding exception.
     *
     * @param message the detail message explaining what caused the exception
     * @param cause   the exception that caused this exception
     */
    public EncodingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
