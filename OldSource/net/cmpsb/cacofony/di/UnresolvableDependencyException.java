package net.cmpsb.cacofony.di;

/**
 * An exception signaling that one or more dependencies are unresolvable.
 *
 * @author Luc Everse
 */
public class UnresolvableDependencyException extends RuntimeException {
    /**
     * Construct a new unresolvable dependency exception with null as its detail message
     * and no cause exception.
     */
    public UnresolvableDependencyException() {
        super();
    }

    /**
     * Construct a new unresolvable dependency exception with a message and no cause exception.
     *
     * @param message the detail message
     */
    public UnresolvableDependencyException(final String message) {
        super(message);
    }

    /**
     * Construct a new unresolvable dependency exception with a cause exception.
     * If the cause exception is non-null, the message will be set to the cause's String value.
     *
     * @param cause the cause exception
     */
    public UnresolvableDependencyException(final Throwable cause) {
        super(cause);
    }

    /**
     * Construct a new unresolvable dependency exception with a message and a cause exception.
     *
     * @param message the detail message
     * @param cause   the cause exception
     */
    public UnresolvableDependencyException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
