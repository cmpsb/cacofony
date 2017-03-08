package net.cmpsb.cacofony;

import net.cmpsb.cacofony.di.DependencyResolver;

/**
 * The server's runtime environment.
 * This is where the framework takes over.
 *
 * If you want to manually instantiate the framework, this is where you specify your custom
 * DI loaders and such.
 *
 * @author Luc Everse
 */
public class Server {
    /**
     * The dependency resolver to use.
     */
    private final DependencyResolver dependencyResolver;

    /**
     * The Jetty server the framework runs on.
     */
    private final org.eclipse.jetty.server.Server server;

    /**
     * Create a new server instance.
     *
     * @param dependencyResolver the dependency resolver to use
     * @param server             the Jetty server the framework runs on
     */
    public Server(final DependencyResolver dependencyResolver,
                  final org.eclipse.jetty.server.Server server) {
        this.dependencyResolver = dependencyResolver;
        this.server = server;
    }
}
