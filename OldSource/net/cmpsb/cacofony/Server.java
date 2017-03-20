package net.cmpsb.cacofony;

import fi.iki.elonen.NanoHTTPD;
import net.cmpsb.cacofony.di.DependencyResolver;
import net.cmpsb.cacofony.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    /**
     * The dependency resolver to use.
     */
    private final DependencyResolver dependencyResolver;

    /**
     * The handler processing incoming requests.
     */
    private final Router handler;

    /**
     * The embedded server to run.
     */
    private final NanoHTTPD server;

    /**
     * Create a new server instance.
     *
     * @param dependencyResolver the dependency resolver to use
     * @param server             the embedded web server to run
     */
    public Server(final DependencyResolver dependencyResolver, final NanoHTTPD server) {
        this.dependencyResolver = dependencyResolver;
        this.handler = this.dependencyResolver.get(Router.class);
        this.server = server;
    }
}
