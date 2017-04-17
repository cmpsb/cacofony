package net.cmpsb.cacofony.exception;

import net.cmpsb.cacofony.controller.Controller;
import net.cmpsb.cacofony.http.exception.HttpException;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.http.response.Response;

/**
 * An exception handler.
 * <p>
 * This class is invoked when any exception is thrown by the request parser or router. The writer
 * is outside of this scope and will terminate the connection if any error occurs.
 * <p>
 * Any subclass will have access to all {@link Controller} features, such as templating.
 *
 * @author Luc Everse
 */
public abstract class ExceptionHandler extends Controller {
    /**
     * Handles an exception that results in a specific HTTP status code.
     *
     * @param request the request that caused the exception, possibly {@code null}
     * @param ex      the exception
     *
     * @return a response
     */
    public abstract Response handle(Request request, HttpException ex);

    /**
     * Handles an exception that is outside the scope of HTTP and should result in an
     * Internal Server Error.
     *
     * @param request the request that caused the exception, possibly {@code null}
     * @param ex      the exception
     *
     * @return a response, preferably with status 500 Internal Server Error
     */
    public abstract Response handle(Request request, Throwable ex);
}
