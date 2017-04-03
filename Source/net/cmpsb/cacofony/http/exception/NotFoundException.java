package net.cmpsb.cacofony.http.exception;

import net.cmpsb.cacofony.http.response.ResponseCode;

/**
 * @author Luc Everse
 */
public class NotFoundException extends HttpException {
    /**
     * Creates a new HTTP exception.
     *
     * @param message the message detailing the problem
     */
    public NotFoundException(final String message) {
        super(ResponseCode.NOT_FOUND, message);
    }
}
