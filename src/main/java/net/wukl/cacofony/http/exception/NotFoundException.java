package net.wukl.cacofony.http.exception;

import net.wukl.cacofony.http.response.ResponseCode;

/**
 * An exception representing a 404 Not Found error.
 *
 * @author Luc Everse
 */
public class NotFoundException extends HttpException {
    /**
     * Creates a new Not Found exception.
     *
     * @param message the message detailing the problem
     */
    public NotFoundException(final String message) {
        super(ResponseCode.NOT_FOUND, message);
    }

    /**
     * Creates a new Not Found exception.
     */
    public NotFoundException() {
        super(ResponseCode.NOT_FOUND);
    }

    /**
     * Creates a new Not Found exception.
     *
     * @param message the message detailing the problem
     * @param cause   the exception that caused this exception
     */
    public NotFoundException(final String message, final Throwable cause) {
        super(ResponseCode.NOT_FOUND, message, cause);
    }

    /**
     * Creates a new Not Found exception.
     *
     * @param cause the exception that caused this exception
     */
    public NotFoundException(final Throwable cause) {
        super(ResponseCode.NOT_FOUND, cause);
    }
}
