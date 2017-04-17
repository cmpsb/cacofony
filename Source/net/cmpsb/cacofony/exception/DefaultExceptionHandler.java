package net.cmpsb.cacofony.exception;

import net.cmpsb.cacofony.http.exception.HttpException;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.http.response.Response;
import net.cmpsb.cacofony.http.response.ResponseCode;
import net.cmpsb.cacofony.http.response.TextResponse;
import net.cmpsb.cacofony.mime.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default exception handler.
 * <p>
 * This handler will log the exception and print a plain text message (not the exception) to the
 * client.
 *
 * @author Luc Everse
 */
public class DefaultExceptionHandler extends ExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    /**
     * Handles an exception that results in a specific HTTP status code.
     *
     * @param request the request that caused the exception
     * @param ex      the exception
     *
     * @return a response
     */
    public Response handle(final Request request, final HttpException ex) {
        logger.warn("HTTP exception: ", ex);

        final String message;
        if (ex.getMessage() == null) {
            message = ex.getCode().getDescription();
        } else {
            message = ex.getMessage();
        }

        final Response response = new TextResponse(message);
        response.setContentType(MimeType.text());
        response.setStatus(ex.getCode());
        response.adoptHeaders(ex.getHeaders());

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
    public Response handle(final Request request, final Throwable ex) {
        logger.error("Internal server error: ", ex);

        final Response response = new TextResponse("Internal Server Error");
        response.setContentType(MimeType.text());
        response.setStatus(ResponseCode.INTERNAL_SERVER_ERROR);

        return response;
    }
}
