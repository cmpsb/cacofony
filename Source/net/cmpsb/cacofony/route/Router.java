package net.cmpsb.cacofony.route;

import net.cmpsb.cacofony.http.exception.NotFoundException;
import net.cmpsb.cacofony.http.request.Method;
import net.cmpsb.cacofony.http.request.MutableRequest;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.mime.MimeParser;
import net.cmpsb.cacofony.mime.MimeType;
import net.cmpsb.cacofony.http.response.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The default handler for incoming HTTP requests.
 *
 * @author Luc Everse
 */
public class Router {
    /**
     * The MIME type parser to use.
     */
    private final MimeParser mimeParser;

    /**
     * A mapping towards routing entries.
     * The first level filters by method, the second level by accept encoding.
     * A single routing entry may appear multiple times in this "tree".
     */
    private final Map<Method, Map<MimeType, List<RoutingEntry>>> routes;

    /**
     * The set of all known routes.
     */
    private final Set<RoutingEntry> allRoutes;

    /**
     * The regex used to split header values.
     */
    private static final Pattern HEADER_COMMA_PATTERN = Pattern.compile(",\\s?");

    /**
     * Creates a new request handler.
     *
     * @param mimeParser the MIME type parser to use
     */
    public Router(final MimeParser mimeParser) {
        this.mimeParser = mimeParser;

        // Build the routing table.
        this.routes = new HashMap<>();
        for (final Method method : Method.values()) {
            this.routes.put(method, new HashMap<>());
        }

        this.allRoutes = new HashSet<>();
    }

    /**
     * Adds a routing entry to the routing table.
     *
     * @param entry  the entry itself
     */
    public void addRoute(final RoutingEntry entry) {
        this.allRoutes.add(entry);

        for (final Method method : entry.getMethods()) {
            final Map<MimeType, List<RoutingEntry>> methodMap = this.routes.get(method);

            for (final MimeType accept : entry.getContentTypes()) {
                final List<RoutingEntry> entries =
                        methodMap.computeIfAbsent(accept, k -> new ArrayList<>());

                entries.add(entry);
            }

            methodMap.computeIfAbsent(MimeType.any(), k -> new ArrayList<>()).add(entry);
        }
    }

    /**
     * Handles an incoming HTTP request.
     * <p>
     * The default handler will try to filter the routes by (1) method and (2) acceptable content
     * type.
     *
     * @param request the incoming request
     *
     * @return a response
     *
     * @throws Throwable any exception thrown by the route
     */
    public Response handle(final MutableRequest request) throws Throwable {
        final List<MimeType> acceptTypes = this.parseAccepts(request);
        Collections.sort(acceptTypes);

        // Look up the mapping of routes for that method.
        final Method method = request.getRealMethod();
        final Response response = this.tryMethod(request, method, acceptTypes);

        if (response != null) {
            return response;
        }

        if (method == Method.HEAD) {
            // If there is no HEAD route, try the same route for GET.
            request.setMethod(Method.GET);
            final Response headResponse = this.tryMethod(request, Method.GET, acceptTypes);
            request.setMethod(Method.HEAD);

            if (headResponse != null) {
                return headResponse;
            }
        }

        throw new NotFoundException("That page or route doesn't exist or cannot be shown.");
    }

    /**
     * Translates the request's Accept headers into a priority queue of acceptable MIME types.
     *
     * @param request the request to process
     *
     * @return an ordered list of acceptable MIME types
     */
    private List<MimeType> parseAccepts(final Request request) {
        final List<String> acceptHeaders = request.getHeaders("Accept");

        if (acceptHeaders == null || acceptHeaders.isEmpty()) {
            return Collections.singletonList(MimeType.any());
        }

        final List<MimeType> acceptTypes = new ArrayList<>();
        for (final String header : acceptHeaders) {
            for (final String item : HEADER_COMMA_PATTERN.split(header)) {
                final MimeType type = this.mimeParser.parse(item);

                acceptTypes.add(type);
            }
        }

        return acceptTypes;
    }

    /**
     * Try to find a route for a given request, target and method.
     *
     * @param request     the original request
     * @param method      the method the route must serve
     * @param acceptTypes the MIME types the client is willing to accept
     *
     * @return a response or {@code null} if no matching route could be found
     *
     * @throws Throwable any exception thrown by the route
     */
    private Response tryMethod(final MutableRequest request,
                               final Method method,
                               final List<MimeType> acceptTypes) throws Throwable {
        final Map<MimeType, List<RoutingEntry>> byAccept = this.routes.get(method);

        // For each content type, try to find the route matching the given path.
        for (final MimeType contentType : acceptTypes) {
            final List<RoutingEntry> possibleRoutes = byAccept.get(contentType);

            // Skip this type if there are no handlers for it.
            if (possibleRoutes == null) {
                continue;
            }

            final Response response = this.tryAccept(request, possibleRoutes, contentType);
            if (response != null) {
                return response;
            }
        }

        return null;
    }

    /**
     * Try to find a route for a given request, target and content type from a list of choices.
     *
     * @param request        the original request
     * @param possibleRoutes a list of routes to try
     * @param contentType    the content type the route must serve
     *
     * @return a response or {@code null} if no matching route could be found
     *
     * @throws Throwable any exception thrown by the route
     */
    private Response tryAccept(final MutableRequest request,
                               final List<RoutingEntry> possibleRoutes,
                               final MimeType contentType) throws Throwable {
        final String target = request.getRawPath();

        for (final RoutingEntry entry : possibleRoutes) {
            final Matcher targetMatcher = entry.getPath().getPattern().matcher(target);

            // The route matches! Serve the request through this route.
            if (targetMatcher.matches()) {
                final Map<String, String> params = entry.getPath().parseParameters(targetMatcher);

                request.setPathParameters(params);
                request.setContentType(contentType);

                return entry.getAction().handle(request);
            }
        }

        return null;
    }
}