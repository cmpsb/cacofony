package net.cmpsb.cacofony.server.host;

import freemarker.template.Configuration;
import net.cmpsb.cacofony.di.DependencyResolver;
import net.cmpsb.cacofony.exception.ExceptionHandler;
import net.cmpsb.cacofony.templating.TemplatingService;
import net.cmpsb.cacofony.templating.freemarker.FreeMarkerTemplatingService;

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
     * Uses FreeMarker as the templating service.
     *
     * @param cfg the FreeMarker configuration
     */
    public void withFreeMarker(final Configuration cfg) {
        this.resolver.add(Configuration.class, cfg);
        this.resolver.implement(TemplatingService.class, FreeMarkerTemplatingService.class);
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
     * Builds the host.
     *
     * @return the host
     */
    public Host build() {
        final Host host = new Host(this.name, this.resolver);

        for (final ControllerPackage pack : this.controllerPackages) {
            host.addControllers(pack);
        }

        return host;
    }
}
