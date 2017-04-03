package net.cmpsb.cacofony.server;

import net.cmpsb.cacofony.controller.ControllerLoader;
import net.cmpsb.cacofony.di.DefaultDependencyResolver;
import net.cmpsb.cacofony.di.DependencyResolver;
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
     * A loader for controllers.
     */
    private final ControllerLoader loader;

    /**
     * Creates a new server.
     *
     * @param resolver the dependency resolver to use
     */
    public Server(final DependencyResolver resolver) {
        this.resolver = resolver;

        this.loader = this.resolver.get(ControllerLoader.class);
    }

    /**
     * Creates a new server with the default dependency resolver.
     */
    public Server() {
        this(new DefaultDependencyResolver());
    }

    /**
     * Scans a package for controllers and services.
     *
     * @param pack the package to scan
     */
    public void scanPackage(final String pack) {
        this.loader.loadAll(pack + ".controller");
    }

    /**
     * Registers an external service.
     *
     * @param type     the service type
     * @param instance the service instance
     * @param <T>      the service type
     */
    public <T> void register(final Class<T> type, final T instance) {
        this.resolver.add(instance, type);
    }

    /**
     * Starts the server.
     *
     * @throws IOException if an I/O exception occurs
     */
    public void start() throws IOException {
        logger.debug("Bootstrapping server.");

        final List<Thread> listeners = new ArrayList<>();

        final ExecutorService pool = Executors.newCachedThreadPool();

        this.resolver.add("resource/server.thread-pool", pool);
        this.resolver.add(new MutableServerSettings(), ServerSettings.class);

        for (final int port : this.httpPorts) {
            logger.debug("{}", port);
            final ServerSocket socket = new ServerSocket(port);
            final Listener listener = this.resolver.get(
                    Ob.map("socket", socket),
                    Listener.class
            );

            final Thread runner = new Thread(listener);
            listeners.add(runner);
            runner.start();
        }

        logger.debug("Bootstrap finished, slumbering.");
    }

    /**
     * Adds a port to the server.
     *
     * @param port        the port number
     * @param isEncrypted whether the connection will be encrypted through TLS
     */
    public void addPort(final int port, final boolean isEncrypted) {
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
}
