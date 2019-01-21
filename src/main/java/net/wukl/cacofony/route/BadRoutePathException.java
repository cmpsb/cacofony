package net.wukl.cacofony.route;

/**
 * An exception signaling that a route path is malformed.
 *
 * @author Luc Everse
 */
public class BadRoutePathException extends RuntimeException {
    /**
     * Construct a new bad route path exception with null as its detail message
     * and no cause exception.
     */
    public BadRoutePathException() {
        super();
    }

    /**
     * Construct a new bad route path exception with a message and no cause exception.
     *
     * @param message the detail message
     */
    public BadRoutePathException(final String message) {
        super(message);
    }

    /**
     * Construct a new bad route path exception with a cause exception.
     * If the cause exception is non-null, the message will be set to the cause's String value.
     *
     * @param cause the cause exception
     */
    public BadRoutePathException(final Throwable cause) {
        super(cause);
    }

    /**
     * Construct a new bad route path exception with a message and a cause exception.
     *
     * @param message the detail message
     * @param cause   the cause exception
     */
    public BadRoutePathException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
