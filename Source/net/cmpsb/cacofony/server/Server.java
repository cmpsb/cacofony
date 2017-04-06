package net.cmpsb.cacofony.server;

import net.cmpsb.cacofony.controller.ControllerLoader;
import net.cmpsb.cacofony.di.DefaultDependencyResolver;
import net.cmpsb.cacofony.di.DependencyResolver;
import net.cmpsb.cacofony.mime.FastMimeParser;
import net.cmpsb.cacofony.mime.MimeParser;
import net.cmpsb.cacofony.templating.DummyTemplatingService;
import net.cmpsb.cacofony.templating.TemplatingService;
import net.cmpsb.cacofony.util.Ob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The main server.
 *
 * @author Luc Everse
 */
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    /**
     * The ports the server should listen on for HTTP connections.
     */
    private final List<Integer> httpPorts = new ArrayList<>();

    /**
     * The ports the server should listen on for HTTPS connections.
     */
    private final List<Integer> httpsPorts = new ArrayList<>();

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
     */
    public Server(final DependencyResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Creates a new server with the default dependency resolver.
     */
    public Server() {
        this(new DefaultDependencyResolver());
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
        this.ensureReady();

        final ControllerLoader loader = this.resolver.get(ControllerLoader.class);
        loader.loadAll(pack + ".controller");
    }

    /**
     * Registers an external service.
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
    public void init() {
        if (!this.resolver.isKnown(MimeParser.class)) {
            this.resolver.get(FastMimeParser.class, MimeParser.class);
        }

        if (!this.resolver.isKnown(ServerSettings.class)) {
            this.resolver.add(new MutableServerSettings(), ServerSettings.class);
        }

        if (!this.resolver.isKnown("resource/server.thread-pool")) {
            final ExecutorService pool = Executors.newCachedThreadPool();
            this.resolver.add("resource/server.thread-pool", pool);
        }

        if (!this.resolver.isKnown(TemplatingService.class)) {
            this.resolver.add(new DummyTemplatingService(), TemplatingService.class);
        }
    }

    /**
     * Returns whether the server is ready for starting or not.
     * <p>
     * If this method returns {@code false}, then {@link #start()} will automatically call
     * {@link #init}.
     *
     * @return true if all dependencies and settings are set, false otherwise
     */
    public boolean isReady() {
        if (!this.resolver.isKnown(MimeParser.class)) {
            return false;
        }

        if (!this.resolver.isKnown(ServerSettings.class)) {
            return false;
        }

        if (!this.resolver.isKnown("resource/server.thread-pool")) {
            return false;
        }

        if (!this.resolver.isKnown(TemplatingService.class)) {
            return false;
        }

        return true;
    }

    /**
     * Starts the server.
     *
     * @throws IOException if an I/O exception occurs
     */
    public void start() throws IOException {
        this.ensureReady();

        logger.debug("Bootstrapping server.");

        for (final int port : this.httpPorts) {
            logger.debug("{}", port);
            final ServerSocket socket = new ServerSocket(port);
            final Listener listener = this.resolver.get(
                    Ob.map("socket", socket),
                    Listener.class
            );

            final Thread runner = new Thread(listener);
            runner.start();
        }

        this.idle = false;
        logger.debug("Bootstrap finished, slumbering.");
    }

    /**
     * Adds a port to the server.
     *
     * @param port        the port number
     * @param isEncrypted whether the connection will be encrypted through TLS
     */
    public void addPort(final int port, final boolean isEncrypted) {
        this.ensureIdle();

        if (isEncrypted) {
            this.httpsPorts.add(port);
        } else {
            this.httpPorts.add(port);
        }
    }

    /**
     * Adds a port to the server.
     * <p>
     * The ports 80 and 8080 are automatically registered as HTTP ports, all other ports
     * are assumed to be HTTPS ports.
     * To control this behavior, use {@link #addPort(int, boolean)}.
     *
     * @param port the port to add
     */
    public void addPort(final int port) {
        this.addPort(port, port != 80 && port != 8080);
    }

    /**
     * Makes sure that the server is not running.
     */
    private void ensureIdle() {
        if (!this.idle) {
            throw new RunningServerException("This operation is not allowed on a running server.");
        }
    }

    /**
     * Makes sure that the server is not running and that all dependencies are there.
     */
    private void ensureReady() {
        this.ensureIdle();

        if (!this.isReady()) {
            this.init();
        }
    }
}
