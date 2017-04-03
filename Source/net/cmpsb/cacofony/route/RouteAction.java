package net.cmpsb.cacofony.route;

import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.http.response.Response;

/**
 * A single controller action bound to a route.
 *
 * @author Luc Everse
 */
@FunctionalInterface
public interface RouteAction {
    /**
     * Handle a request.
     *
     * @param request the HTTP request
     *
     * @return a response
     *
     * @throws Throwable any exception
     */
    Response handle(Request request) throws Throwable;
}
