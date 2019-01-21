package net.wukl.cacofony.server.host;

import net.wukl.cacofony.controller.ControllerLoader;
import net.wukl.cacofony.exception.ExceptionHandler;
import net.wukl.cacofony.http.response.ResponsePreparer;
import net.wukl.cacofony.http.response.ResponseWriter;
import net.wukl.cacofony.route.ResourceFileRouteFactory;
import net.wukl.cacofony.route.Router;
import net.wukl.cacofony.route.RoutingEntry;
import net.wukl.cacofony.route.StaticFileRouteFactory;
import net.wukl.cacodi.DependencyResolver;

import java.nio.file.Path;

/**
 * A host to serve.
 *
 * @author Luc Everse
 */
public class Host {
    /**
     * The host name.
     */
    private final String name;

    /**
     * The dependency resolver for this host.
     */
    private final DependencyResolver resolver;

    /**
     * The host's private router.
     */
    private final Router router;

    /**
     * The host's private exception handler.
     */
    private final ExceptionHandler exceptionHandler;

    /**
     * The host's private response preparer.
     */
    private final ResponsePreparer responsePreparer;

    /**
     * The host's private response writer.
     */
    private final ResponseWriter responseWriter;

    /**
     * The controller loader to use.
     */
    private final ControllerLoader controllerLoader;

    /**
     * Creates a new host.
     *
     * @param name     the hostname
     * @param resolver the dependency resolver to use
     */
    public Host(final String name, final DependencyResolver resolver) {
        this.name = name;
        this.resolver = resolver;

        this.router = this.resolver.get(Router.class);
        this.exceptionHandler = this.resolver.get(ExceptionHandler.class);
        this.responsePreparer = this.resolver.get(ResponsePreparer.class);
        this.responseWriter = this.resolver.get(ResponseWriter.class);
        this.controllerLoader = this.resolver.get(ControllerLoader.class);
    }

    /**
     * Returns the hostname.
     *
     * @return the hostname
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the private dependency resolver.
     *
     * @return the resolver
     */
    public DependencyResolver getResolver() {
        return this.resolver;
    }

    /**
     * Returns the private router.
     *
     * @return the router
     */
    public Router getRouter() {
        return this.router;
    }

    /**
     * Returns the private exception handler.
     *
     * @return the exception handler
     */
    public ExceptionHandler getExceptionHandler() {
        return this.exceptionHandler;
    }

    /**
     * Returns the private response preparer.
     *
     * @return the response preparer
     */
    public ResponsePreparer getResponsePreparer() {
        return this.responsePreparer;
    }

    /**
     * Returns the private response writer.
     *
     * @return the response writer
     */
    public ResponseWriter getResponseWriter() {
        return this.responseWriter;
    }

    /**
     * Loads a package containing controllers.
     *
     * @param pack the package
     */
    public void addControllers(final ControllerPackage pack) {
        this.controllerLoader.loadAll(pack.getPrefix(), pack.getPack());
    }

    /**
     * Adds a route that serves static files.
     *
     * @param prefix the URL prefix for the routes
     * @param dir    the local directory the files are in
     */
    public void addStaticFiles(final String prefix, final Path dir) {
        final StaticFileRouteFactory factory = this.resolver.get(StaticFileRouteFactory.class);
        final RoutingEntry entry = factory.build(prefix, dir);
        this.router.addRoute(entry);
    }

    /**
     * Adds a route that serves jar resources.
     *
     * @param prefix the URL prefix for the routes
     * @param jar    the jar the resources are in
     * @param dir    the directory inside the jar
     */
    public void addStaticResources(final String prefix, final Class<?> jar, final String dir) {
        final ResourceFileRouteFactory factory = this.resolver.get(ResourceFileRouteFactory.class);
        final RoutingEntry entry = factory.build(prefix, jar, dir);
        this.router.addRoute(entry);
    }
}
