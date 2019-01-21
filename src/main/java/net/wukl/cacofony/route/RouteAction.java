package net.wukl.cacofony.route;

import net.wukl.cacofony.http.request.Request;
import net.wukl.cacofony.http.response.Response;

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
     * @throws Exception any exception
     */
    Response handle(Request request) throws Exception;
}
