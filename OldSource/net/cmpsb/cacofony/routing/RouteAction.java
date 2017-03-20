package net.cmpsb.cacofony.routing;

import net.cmpsb.cacofony.http.Request;

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
     */
    Object handle(Request request);
}
