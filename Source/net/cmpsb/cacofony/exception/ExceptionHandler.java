package net.cmpsb.cacofony.exception;

import net.cmpsb.cacofony.http.exception.HttpException;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.http.response.Response;
import net.cmpsb.cacofony.http.response.ResponseCode;
import net.cmpsb.cacofony.http.response.TextResponse;
import net.cmpsb.cacofony.mime.MimeType;

/**
 * The standard exception handler.
 *
 * @author Luc Everse
 */
public class ExceptionHandler {
    /**
     * Handles an exception that results in a specific HTTP status code.
     *
     * @param request the request that caused the exception
     * @param ex      the exception
     *
     * @return a response
     */
    public Response handle(final Request request, final HttpException ex) {
        final Response response = new TextResponse(ex.getMessage());
        response.setContentType(MimeType.text());
        response.setStatus(ex.getCode());

        return response;
    }

    /**
     * Handles an exception that is outside the scope of HTTP and should result in an
     * Internal Server Error.
     *
     * @param request the request that caused the exception
     * @param ex      the exception
     *
     * @return a response, preferably with status 500 Internal Server Error
     */
    public Response handle(final Request request, final Exception ex) {
        final Response response = new TextResponse("Internal Server Error");
        response.setContentType(MimeType.text());
        response.setStatus(ResponseCode.INTERNAL_SERVER_ERROR);

        return response;
    }
}
