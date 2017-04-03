package net.cmpsb.cacofony.http.exception;

import net.cmpsb.cacofony.http.response.ResponseCode;

/**
 * A specific HTTP exception signaling a bad HTTP request.
 *
 * @author Luc Everse
 */
public class BadRequestException extends HttpException {
    /**
     * Creates a new Bad Request exception.
     */
    public BadRequestException() {
        super(ResponseCode.BAD_REQUEST);
    }

    /**
     * Creates a new Bad Request exception.
     *
     * @param message the detail message
     */
    public BadRequestException(final String message) {
        super(ResponseCode.BAD_REQUEST, message);
    }

    /**
     * Creates a new Bad Request exception.
     *
     * @param cause the exception that caused this exception
     */
    public BadRequestException(final Throwable cause) {
        super(ResponseCode.BAD_REQUEST, cause);
    }

    /**
     * Creates a new Bad Request exception.
     *
     * @param message the detail message
     * @param cause   the exception that caused this exception
     */
    public BadRequestException(final String message, final Throwable cause) {
        super(ResponseCode.BAD_REQUEST, message, cause);
    }
}
