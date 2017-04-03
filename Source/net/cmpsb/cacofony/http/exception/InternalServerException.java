package net.cmpsb.cacofony.http.exception;

import net.cmpsb.cacofony.http.response.ResponseCode;

/**
 * @author Luc Everse
 */
public class InternalServerException extends HttpException {
    /**
     * Creates a new internal server exception.
     */
    public InternalServerException() {
        super(ResponseCode.INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates a new internal server exception.
     *
     * @param message the detail message
     */
    public InternalServerException(final String message) {
        super(ResponseCode.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * Creates a new internal server exception.
     *
     * @param cause the exception that caused this exception
     */
    public InternalServerException(final Throwable cause) {
        super(ResponseCode.INTERNAL_SERVER_ERROR, cause);
    }

    /**
     * Creates a new internal server exception.
     *
     * @param message the detail message
     * @param cause   the exception that caused this exception
     */
    public InternalServerException(final String message, final Throwable cause) {
        super(ResponseCode.INTERNAL_SERVER_ERROR, message, cause);
    }
}
