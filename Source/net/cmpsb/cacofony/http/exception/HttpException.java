package net.cmpsb.cacofony.http.exception;

import net.cmpsb.cacofony.http.response.ResponseCode;

/**
 * An exception that has a meaning in the HTTP context.
 * These exceptions define a corresponding status code.
 *
 * @author Luc Everse
 */
public class HttpException extends RuntimeException {
    /**
     * The HTTP status code.
     */
    private final ResponseCode code;

    /**
     * Create a new HTTP exception.
     *
     * @param code    the HTTP status code
     */
    public HttpException(final ResponseCode code) {
        this.code = code;
    }

    /**
     * Create a new HTTP exception.
     *
     * @param code    the HTTP status code
     * @param message the detail message
     */
    public HttpException(final ResponseCode code, final String message) {
        super(message);
        this.code = code;
    }

    /**
     * Create a new HTTP exception.
     *
     * @param code    the HTTP status code
     * @param cause   the exception that caused this exception
     */
    public HttpException(final ResponseCode code, final Throwable cause) {
        super(cause);
        this.code = code;
    }

    /**
     * Create a new HTTP exception.
     *
     * @param code    the HTTP status code
     * @param message the detail message
     * @param cause   the exception that caused this exception
     */
    public HttpException(final ResponseCode code, final String message, final Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * @return the HTTP status code this exception represents
     */
    public ResponseCode getCode() {
        return this.code;
    }
}
