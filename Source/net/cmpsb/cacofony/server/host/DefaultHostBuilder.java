package net.cmpsb.cacofony.server.host;

import net.cmpsb.cacofony.controller.Controller;
import net.cmpsb.cacofony.http.request.Method;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.http.response.EmptyResponse;
import net.cmpsb.cacofony.http.response.Response;
import net.cmpsb.cacofony.http.response.ResponseCode;
import net.cmpsb.cacofony.mime.MimeType;
import net.cmpsb.cacofony.route.CompiledPath;
import net.cmpsb.cacofony.route.RoutingEntry;
import net.wukl.cacodi.DependencyResolver;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * A builder that builds Hosts that always reply a 404.
 *
 * @author Luc Everse
 */
public class DefaultHostBuilder extends HostBuilder {
    /**
     * The dependency resolver.
     */
    private final DependencyResolver resolver;

    /**
     * Creates a new host builder.
     *
     * @param name     the host name
     * @param resolver the dependency resolver cloned from the server
     */
    public DefaultHostBuilder(final String name, final DependencyResolver resolver) {
        super(name, resolver);
        this.resolver = resolver;
    }

    /**
     * Returns a host returning a 404 regardless of the actual request path.
     *
     * @return a host
     */
    @Override
    public Host build() {
        final Host host = new Host("*", this.resolver);

        final Controller controller = new NotFoundController();
        final java.lang.reflect.Method method;
        try {
            method = NotFoundController.class.getMethod("handle", Request.class);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        final Pattern anyPattern = Pattern.compile("(?<PATH>.*)(?<QUERY>.*)");
        final CompiledPath path = new CompiledPath("", anyPattern, Collections.emptyList());
        final RoutingEntry entry = new RoutingEntry(
            "catch_all",
            path,
            controller,
            method,
            Arrays.asList(Method.values()),
            Collections.singletonList(MimeType.any())
        );

        host.getRouter().addRoute(entry);

        return host;
    }

    /**
     * The controller responding Not Found to any request.
     */
    public class NotFoundController extends Controller {
        /**
         * Handles any request and replies a 404.
         *
         * @param request the request
         *
         * @return a Not Found response
         */
        public Response handle(final Request request) {
            final Response response = new EmptyResponse(ResponseCode.NOT_FOUND);
            response.setContentType(MimeType.text());
            return response;
        }
    }
}
