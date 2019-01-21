package net.wukl.cacofony.server;

/**
 * An exception that signals that the current operation is not valid for a running server.
 * <p>
 * This exception gets thrown if the user tries to adjust certain settings and dependencies after
 * calling {@link Server#start()}.
 *
 * @author Luc Everse
 */
public class RunningServerException extends RuntimeException {
    /**
     * Creates a new Running Server Exception.
     */
    public RunningServerException() {
        super();
    }

    /**
     * Creates a new Running Server Exception.
     *
     * @param message a message containing details on the exception
     */
    public RunningServerException(final String message) {
        super(message);
    }

    /**
     * Creates a new Running Server Exception.
     *
     * @param cause the exception that caused this exception
     */
    public RunningServerException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new Running Server Exception.
     *
     * @param message a message containing details on the exception
     * @param cause   the exception that caused this exception
     */
    public RunningServerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
