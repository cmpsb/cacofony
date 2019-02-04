package net.wukl.cacofony.http2.hpack;

/**
 * An exception signaling a problem occurred while initializing the static HPACK table.
 */
public class HpackInitializationException extends RuntimeException {
    /**
     * Creates a new HPACK initialization exception.
     */
    public HpackInitializationException() {
        super();
    }

    /**
     * Creates a new HPACK initialization exception.
     *
     * @param message the detail message explaining what caused the exception
     */
    public HpackInitializationException(final String message) {
        super(message);
    }

    /**
     * Creates a new HPACK initialization exception.
     *
     * @param cause the exception that caused this exception
     */
    public HpackInitializationException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new HPACK initialization exception.
     *
     * @param message the detail message explaining what caused the exception
     * @param cause   the exception that caused this exception
     */
    public HpackInitializationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
