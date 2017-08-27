package net.cmpsb.cacofony.route;

import net.cmpsb.cacofony.http.request.MutableRequest;
import net.cmpsb.cacofony.http.response.Response;

/**
 * The part of the router that takes care of passing the correct parameters to an action.
 *
 * @author Luc Everse
 */
public class ActionInvoker {
    /**
     * Invokes the action that corresponds to a request under the given conditions.
     *
     * @param entry   the routing entry to invoke
     * @param request the request to invoke the entry with
     *
     * @return the entry's response
     *
     * @throws Exception any exception thrown by the route
     */
    public Response invoke(final RoutingEntry entry, final MutableRequest request)
            throws Exception {

        return (Response) entry.invoke(request);
    }
}
