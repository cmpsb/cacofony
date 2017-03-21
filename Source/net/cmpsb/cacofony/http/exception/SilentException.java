package net.cmpsb.cacofony.http.exception;

import net.cmpsb.cacofony.http.response.ResponseCode;

/**
 * An exception that causes the connection to terminate without any error response.
 * <p>
 * This exception is an HTTP exception with the response code Bad Request.
 * Normal handling should NOT rely on this.
 * <p>
 * The main usage for these is when the client sends an incomplete request.
 *
 * @author Luc Everse
 */
public class SilentException extends HttpException {
    /**
     * Create a new silent exception.
     *
     * @param message the detail message
     */
    public SilentException(final String message) {
        super(ResponseCode.BAD_REQUEST, message);
    }

    /**
     * Create a new silent exception.
     *
     * @param cause the exception that caused this exception
     */
    public SilentException(final Throwable cause) {
        super(ResponseCode.BAD_REQUEST, cause);
    }

    /**
     * Create a new silent exception.
     *
     * @param message the detail message
     * @param cause   the exception that caused this exception
     */
    public SilentException(final String message, final Throwable cause) {
        super(ResponseCode.BAD_REQUEST, message, cause);
    }
}
