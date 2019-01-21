package net.wukl.cacofony.http.exception;

import net.wukl.cacofony.http.response.ResponseCode;

/**
 * A specific HTTP exception signaling a that the server doesn't implement the requested
 * functionality.
 *
 * @author Luc Everse
 */
public class NotImplementedException extends HttpException {
    /**
     * Create a new Not Implemented exception.
     *
     * @param message the detail message
     */
    public NotImplementedException(final String message) {
        super(ResponseCode.NOT_IMPLEMENTED, message);
    }

    /**
     * Create a new Not Implemented exception.
     *
     * @param cause the exception that caused this exception
     */
    public NotImplementedException(final Throwable cause) {
        super(ResponseCode.NOT_IMPLEMENTED, cause);
    }

    /**
     * Create a new Not Implemented exception.
     *
     * @param message the detail message
     * @param cause   the exception that caused this exception
     */
    public NotImplementedException(final String message, final Throwable cause) {
        super(ResponseCode.NOT_IMPLEMENTED, message, cause);
    }
}
