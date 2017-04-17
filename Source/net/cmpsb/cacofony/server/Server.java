package net.cmpsb.cacofony.server;

import net.cmpsb.cacofony.controller.ControllerLoader;
import net.cmpsb.cacofony.di.DefaultDependencyResolver;
import net.cmpsb.cacofony.di.DependencyResolver;
import net.cmpsb.cacofony.exception.DefaultExceptionHandler;
import net.cmpsb.cacofony.exception.ExceptionHandler;
import net.cmpsb.cacofony.mime.FastMimeParser;
import net.cmpsb.cacofony.mime.MimeDb;
import net.cmpsb.cacofony.mime.MimeDbLoader;
import net.cmpsb.cacofony.mime.MimeParser;
import net.cmpsb.cacofony.route.ResourceFileRouteFactory;
import net.cmpsb.cacofony.route.Router;
import net.cmpsb.cacofony.route.RoutingEntry;
import net.cmpsb.cacofony.route.StaticFileRouteFactory;
import net.cmpsb.cacofony.templating.DummyTemplatingService;
import net.cmpsb.cacofony.templating.TemplatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;

/**
 * The main server.
 *
 * @author Luc Everse
 */
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    /**
     * The server's settings.
     */
    private final ServerSettings settings;

    /**
     * The dependency resolver to use.
     */
    private final DependencyResolver resolver;

    /**
     * Whether the server is idle or running.
     */
    private boolean idle = true;

    /**
     * Creates a new server.
     *
     * @param resolver the dependency resolver to use
     * @param settings the server's settings
     */
    public Server(final DependencyResolver resolver, final ServerSettings settings) {
        this.resolver = resolver;
        this.settings = settings;

        this.resolver.add(settings, ServerSettings.class);

        this.init();
    }

    /**
     * Creates a new server.
     *
     * @param settings the server's settings
     */
    public Server(final ServerSettings settings) {
        this(new DefaultDependencyResolver(), settings);
    }

    /**
     * Creates a new server with the default dependency resolver and settings.
     */
    public Server() {
        this(new MutableServerSettings());
    }

    /**
     * Returns the dependency resolver the server is using.
     *
     * @return the dependency resolver
     */
    public DependencyResolver getResolver() {
        return this.resolver;
    }

    /**
     * Scans a package for controllers and services.
     *
     * @param pack the package to scan
     */
    public void scanPackage(final String pack) {
        this.ensureIdle();

        final ControllerLoader loader = this.resolver.get(ControllerLoader.class);
        loader.loadAll(pack + ".controller");
    }

    /**
     * Register a prefix for serving static files from disk.
     *
     * @param prefix the URL prefix static files should be behind
     * @param dir    the local directory the static files are in
     */
    public void addStaticFiles(final String prefix, final String dir) {
        this.ensureIdle();

        final StaticFileRouteFactory factory = this.resolver.get(StaticFileRouteFactory.class);

        final Router router = this.resolver.get(Router.class);

        final RoutingEntry entry = factory.build(prefix, Paths.get(dir));
        router.addRoute(entry);
    }

    /**
     * Registers a prefix for serving files from a jar.
     *
     * @param prefix the URL prefix all static files should be behind
     * @param jar    the jar the resources should be in
     * @param dir    the directory inside the jar the resources should be in
     */
    public void addStaticResources(final String prefix, final Class<?> jar, final String dir) {
        this.ensureIdle();

        final ResourceFileRouteFactory factory = this.resolver.get(ResourceFileRouteFactory.class);

        final Router router = this.resolver.get(Router.class);

        final RoutingEntry entry = factory.build(prefix, jar, dir);
        router.addRoute(entry);
    }

    /**
     * Registers an external service.
     * <p>
     * If the service is meant to be a server dependency (such as internal parsers, factories,
     * etc.), please register them with the resolver <em>before</em> constructing the server
     * object. Otherwise the server may not pick up your override.
     *
     * @param type     the service type
     * @param instance the service instance
     * @param <T>      the service type
     */
    public <T> void register(final Class<T> type, final T instance) {
        this.ensureIdle();

        this.resolver.add(instance, type);
    }

    /**
     * Initializes the server.
     * <p>
     * This method fills in the missing dependencies and sets many settings to their defaults.
     */
    private void init() {
        // Add the default 80 and 443 ports if none are set.
        final Set<Port> ports = this.settings.getPorts();
        if (ports.isEmpty()) {
            ports.add(new Port(80, false));
            ports.add(new Port(443, true));
        }

        if (!this.resolver.isKnown(ExceptionHandler.class)) {
            this.resolver.add(new DefaultExceptionHandler(), ExceptionHandler.class);
        }

        if (!this.resolver.isKnown(SSLServerSocketFactory.class)) {
            this.resolver.add((SSLServerSocketFactory) SSLServerSocketFactory.getDefault(),
                    SSLServerSocketFactory.class);
        }

        if (!this.resolver.isKnown(MimeParser.class)) {
            this.resolver.get(FastMimeParser.class, MimeParser.class);
        }

        if (!this.resolver.isKnown(TemplatingService.class)) {
            this.resolver.add(new DummyTemplatingService(), TemplatingService.class);
        }

        if (!this.resolver.isKnown(ListenerFactory.class)) {
            this.resolver.get(DefaultListenerFactory.class, ListenerFactory.class);
        }

        if (!this.resolver.isKnown(MimeDb.class)) {
            final MimeDb db = new MimeDb();
            final MimeDbLoader loader = this.resolver.get(MimeDbLoader.class);
            loader.load(this.getClass().getResourceAsStream("/net/cmpsb/cacofony/mime.types"),
                        db::register);
            this.resolver.add(db);
        }
    }

    /**
     * Starts the server.
     *
     * @throws IOException if an I/O exception occurs
     */
    public void start() throws IOException {
        this.ensureIdle();

        logger.debug("Bootstrapping server.");

        final ListenerFactory factory = this.resolver.get(ListenerFactory.class);
        for (final Port port : this.settings.getPorts()) {
            factory.boot(port);
        }

        this.idle = false;
        logger.debug("Bootstrap finished, slumbering.");
    }

    /**
     * Makes sure that the server is not running.
     */
    private void ensureIdle() {
        if (!this.idle) {
            throw new RunningServerException("This operation is not allowed on a running server.");
        }
    }
}
