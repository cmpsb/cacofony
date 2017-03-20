package net.cmpsb.cacofony.routing;

import fi.iki.elonen.NanoHTTPD;
import net.cmpsb.cacofony.http.Method;
import net.cmpsb.cacofony.http.Request;
import net.cmpsb.cacofony.response.Response;
import net.cmpsb.cacofony.util.ListHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    /**
     * A mapping towards routing entries.
     * The first level filters by method, the second level by accept encoding.
     * A single routing entry may appear multiple times in this "tree".
     */
    private final Map<Method, Map<String, List<RoutingEntry>>> routes;

    /**
     * The set of all known routes.
     */
    private final Set<RoutingEntry> allRoutes;

    /**
     * The regex used to split header values.
     */
    private static final Pattern HEADER_COMMA_PATTERN = Pattern.compile(",\\s?");

    /**
     * The regex used to split the weight from the actual type in an accept type.
     */
    private static final Pattern ACCEPT_WEIGHT_PATTERN = Pattern.compile(";");

    /**
     * Create a new request handler.
     */
    public Router() {
        // Build the routing table.
        this.routes = new HashMap<>();
        for (final Method method : Method.values()) {
            this.routes.put(method, new HashMap<>());
        }

        this.allRoutes = new HashSet<>();
    }

    /**
     * Add a routing entry to the routing table.
     *
     * @param entry  the entry itself
     */
    public void addRoute(final RoutingEntry entry) {
        this.allRoutes.add(entry);

        for (final Method method : entry.getMethods()) {
            final Map<String, List<RoutingEntry>> methodMap = this.routes.get(method);

            for (final String accept : entry.getTransformer().getContentTypes()) {
                final List<RoutingEntry> entries =
                        methodMap.computeIfAbsent(accept, k -> new ArrayList<>());

                entries.add(entry);
            }
        }
    }

    /**
     * Handle an incoming HTTP request.
     *
     * The default handler will try to filter the routes by (1) method and (2) acceptable content
     * type.
     *
     * @param request
     *
     * @throws IOException      if an I/O exception occurs while writing the request
     * @throws ServletException a general exception when handling a request
     */
    @Override
    public void handle(final NanoHTTPD.IHTTPSession request)
            throws IOException, ServletException {

        // Parse the request method.
        final Method method;
        try {
            method = Method.get(baseRequest.getMethod().toUpperCase());
        } catch (final IllegalArgumentException ex) {
            // The method is not a real HTTP method.
            response.setStatus(400);
            return;
        }

        // Look up the mapping of routes for that method.
        final Map<String, List<RoutingEntry>> byAccept = this.routes.get(method);

        // Gather which content types the client requests.
        final List<String> acceptHeaders =
                ListHelpers.fromEnumeration(baseRequest.getHeaders("Accept"));

        final LinkedHashSet<String> acceptTypes = new LinkedHashSet<>();
        for (final String header : acceptHeaders) {
            for (final String item : HEADER_COMMA_PATTERN.split(header)) {
                final String type = ACCEPT_WEIGHT_PATTERN.split(item)[0];
                acceptTypes.add(type);
            }
        }

        // For each content type, try to find the route matching the given path.
        for (final String contentType : acceptTypes) {
            final List<RoutingEntry> possibleRoutes = byAccept.get(contentType);

            // Skip this type if there are no handlers for it.
            if (possibleRoutes == null) {
                continue;
            }

            for (final RoutingEntry entry : possibleRoutes) {
                final Matcher targetMatcher = entry.getPath().getPattern().matcher(target);

                // The route matches! Serve the request through this route.
                if (targetMatcher.matches()) {
                    this.serve(baseRequest, contentType, entry, targetMatcher, response);
                    return;
                }
            }
        }

        // No routes match, reply a 404.
        response.setStatus(404);
    }

    /**
     * Serve a request through a routing action.
     *
     * @param baseRequest    the base request to serve
     * @param contentType    the content type the response contains
     * @param entry          the chosen routing entry
     * @param routeMatcher   the pattern matcher used to match the URI
     * @param responseTarget the object to write the response to
     *
     * @throws IOException if something goes wrong while writing the response
     */
    private void serve(final org.eclipse.jetty.server.Request baseRequest,
                       final String contentType,
                       final RoutingEntry entry,
                       final Matcher routeMatcher,
                       final HttpServletResponse responseTarget) throws IOException {
        // Transfer the match groups into something the user will understand.
        final Map<String, String> routeParams = new HashMap<>();
        entry.getPath().getParameters().forEach(p -> routeParams.put(p, routeMatcher.group(p)));

        final Request request = new Request(baseRequest, routeParams);

        final Object rawResponse = entry.getAction().handle(request);

        // Copy the content type from the Accept header.
        responseTarget.setContentType(contentType);

        final PrintWriter out = responseTarget.getWriter();

        // If the response is the more complex response object, copy some of the special fields.
        // Also invoke the response-specific transformer.
        if (rawResponse instanceof Response) {
            final Response response = (Response) rawResponse;

            entry.getTransformer().transform(response, out);

            responseTarget.setStatus(response.getStatus());
        } else {
            entry.getTransformer().transform(rawResponse, out);

            responseTarget.setStatus(200);
        }

        out.flush();
    }
}
