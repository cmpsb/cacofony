package net.wukl.cacofony.server.host;

import freemarker.template.Configuration;
import net.wukl.cacofony.exception.ExceptionHandler;
import net.wukl.cacofony.server.ServerSettings;
import net.wukl.cacofony.templating.TemplatingService;
import net.wukl.cacofony.templating.freemarker.FreeMarkerService;
import net.wukl.cacodi.DependencyResolver;
import net.wukl.cacodi.Factory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Luc Everse
 */
public class HostBuilder {
    /**
     * The hostname.
     */
    private final String name;

    /**
     * The dependency resolver.
     */
    private final DependencyResolver resolver;

    /**
     * The packages containing the controllers.
     */
    private final List<ControllerPackage> controllerPackages = new ArrayList<>();

    /**
     * The static routes.
     */
    private final List<StaticRouteEntry> staticRoutes = new ArrayList<>();

    /**
     * The server settings to use instead of the global settings.
     */
    private ServerSettings settings = null;

    /**
     * Creates a new host builder.
     *
     * @param name the host name
     * @param resolver the dependency resolver cloned from the server
     */
    public HostBuilder(final String name, final DependencyResolver resolver) {
        this.name = name;
        this.resolver = resolver;
    }

    /**
     * Sets the exception handler.
     *
     * @param exceptionHandler the exception handler class
     */
    public void setExceptionHandler(final Class<? extends ExceptionHandler> exceptionHandler) {
        this.resolver.implement(ExceptionHandler.class, exceptionHandler);
    }

    /**
     * Sets the templating service.
     *
     * @param service the templating service
     */
    public void setTemplatingService(final Class<? extends TemplatingService> service) {
        this.resolver.implement(TemplatingService.class, service);
    }

    /**
     * Sets the factory for the templating service.
     *
     * @param factory the factory
     */
    public void setTemplatingServiceFactory(final Factory<TemplatingService> factory) {
        this.resolver.addFactory(TemplatingService.class, factory);
    }

    /**
     * Uses FreeMarker as the templating service.
     *
     * @param cfg the FreeMarker configuration
     */
    public void withFreeMarker(final Configuration cfg) {
        this.resolver.add(Configuration.class, cfg);
        this.resolver.implement(TemplatingService.class, FreeMarkerService.class);
    }

    /**
     * Adds a package containing controllers.
     *
     * @param pack the package name
     */
    public void addControllers(final String pack) {
        this.controllerPackages.add(new ControllerPackage(pack));
    }

    /**
     * Adds a package containing controllers.
     *
     * @param pack   the package name
     * @param prefix the path prefix
     */
    public void addControllers(final String pack, final String prefix) {
        this.controllerPackages.add(new ControllerPackage(pack, prefix));
    }

    /**
     * Adds a route that serves static files.
     *
     * @param prefix the URL prefix for the routes
     * @param dir    the local directory the files are in
     */
    public void addStaticFiles(final String prefix, final String dir) {
        this.staticRoutes.add(new StaticRouteEntry(prefix, dir));
    }

    /**
     * Adds a route that serves jar resources.
     *
     * @param prefix the URL prefix for the routes
     * @param jar    the jar the resources are in
     * @param dir    the directory inside the jar
     */
    public void addStaticResources(final String prefix, final Class<?> jar, final String dir) {
        this.staticRoutes.add(new StaticRouteEntry(prefix, dir, jar));
    }

    /**
     * Overrides the server settings for this host.
     *
     * @param settings the settings
     */
    public void setSettings(final ServerSettings settings) {
        this.settings = settings;
    }

    /**
     * Builds the host.
     *
     * @return the host
     */
    public Host build() {
        if (this.settings != null) {
            this.resolver.add(ServerSettings.class, this.settings);
        }

        final Host host = new Host(this.name, this.resolver);

        for (final ControllerPackage pack : this.controllerPackages) {
            host.addControllers(pack);
        }

        for (final StaticRouteEntry route : this.staticRoutes) {
            if (route.getJar() == null) {
                host.addStaticFiles(route.getPrefix(), Paths.get(route.getPath()));
            } else {
                host.addStaticResources(route.getPrefix(), route.getJar(), route.getPath());
            }
        }

        return host;
    }
}
