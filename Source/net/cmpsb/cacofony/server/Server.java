package net.cmpsb.cacofony.server;

import net.cmpsb.cacofony.di.DependencyResolver;
import net.cmpsb.cacofony.server.host.DefaultHostBuilder;
import net.cmpsb.cacofony.server.host.Host;
import net.cmpsb.cacofony.server.host.HostBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
     * All returned host builders.
     */
    private final List<HostBuilder> hostBuilders = new ArrayList<>();

    /**
     * The builder for the default host.
     */
    private HostBuilder defaultHostBuilder;

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

        this.resolver.add(ServerSettings.class, settings);

        this.defaultHostBuilder = new DefaultHostBuilder("", new DependencyResolver(resolver));
    }

    /**
     * Creates a new server.
     *
     * @param settings the server's settings
     */
    public Server(final ServerSettings settings) {
        this(new DependencyResolver(), settings);
    }

    /**
     * Creates a new server with the default dependency resolver and settings.
     */
    public Server() {
        this(new MutableServerSettings());
    }

    /**
     * Adds a host to the server.
     * <p>
     * This returns a host builder where you can customize the host's settings. You don't have to
     * do anything with it afterwards - as soon as the server starts all builders will called.
     *
     * @param hostname the name of the host
     *
     * @return a host builder
     */
    public HostBuilder addHost(final String hostname) {
        this.ensureIdle();

        final DependencyResolver resolverClone = new DependencyResolver(this.resolver);
        final HostBuilder builder = new HostBuilder(hostname, resolverClone);

        this.hostBuilders.add(builder);

        return builder;
    }
    /**
     * Adds a host to the server. The host will serve as the catch-all default host.
     * <p>
     * This returns a host builder where you can customize the host's settings. You don't have to
     * do anything with it afterwards - as soon as the server starts all builders will called.
     *
     * @param hostname the name of the host
     *
     * @return a host builder
     */
    public HostBuilder addDefaultHost(final String hostname) {
        this.ensureIdle();

        final DependencyResolver resolverClone = new DependencyResolver(this.resolver);
        final HostBuilder builder = new HostBuilder(hostname, resolverClone);

        this.defaultHostBuilder = builder;
        return builder;
    }

    /**
     * Starts the server.
     *
     * @throws IOException if an I/O exception occurs
     */
    public void run() throws IOException {
        this.ensureIdle();

        logger.debug("Bootstrapping server.");

        final ConnectionHandler handler = this.resolver.get(ConnectionHandler.class);
        for (final HostBuilder builder : this.hostBuilders) {
            final Host host = builder.build();
            logger.debug("Built host {}.", host.getName());
            handler.addHost(host);
        }
        handler.setDefaultHost(this.defaultHostBuilder.build());

        final ListenerFactory factory = this.resolver.get(ListenerFactory.class);
        for (final Port port : this.settings.getPorts()) {
            final Listener listener = factory.build(port);
            new Thread(listener).start();
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
