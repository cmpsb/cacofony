package net.cmpsb.cacofony.server;

import net.cmpsb.cacofony.di.DependencyResolver;
import net.cmpsb.cacofony.exception.DefaultExceptionHandler;
import net.cmpsb.cacofony.exception.ExceptionHandler;
import net.cmpsb.cacofony.mime.FastMimeParser;
import net.cmpsb.cacofony.mime.MimeDb;
import net.cmpsb.cacofony.mime.MimeDbLoader;
import net.cmpsb.cacofony.mime.MimeParser;
import net.cmpsb.cacofony.templating.DummyTemplatingService;
import net.cmpsb.cacofony.templating.TemplatingService;

import javax.net.ssl.SSLServerSocketFactory;
import java.util.Set;

/**
 * A builder for the Server class.
 *
 * @author Luc Everse
 */
public class ServerBuilder {
    /**
     * The dependency resolver to use.
     */
    private final DependencyResolver resolver;

    /**
     * The server socket factory.
     */
    private SSLServerSocketFactory socketFactory =
            (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

    /**
     * The server listener factory.
     */
    private Class<? extends ListenerFactory> listenerFactory = DefaultListenerFactory.class;

    /**
     * The server settings.
     */
    private ServerSettings settings = new MutableServerSettings();

    /**
     * Creates a new server builder.
     *
     * @param resolver the dependency resolver
     */
    public ServerBuilder(final DependencyResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Creates a new server builder.
     */
    public ServerBuilder() {
        this(new DependencyResolver());
    }

    /**
     * Sets the socket factory.
     *
     * @param socketFactory the socket factory
     */
    public void setSocketFactory(final SSLServerSocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    /**
     * Sets the server's configuration.
     *
     * @param settings the settings
     */
    public void setSettings(final ServerSettings settings) {
        this.settings = settings;
    }

    /**
     * Builds the server.
     *
     * @return the server
     */
    public Server build() {
        // Add the default 80 and 443 ports if none are set.
        final Set<Port> ports = this.settings.getPorts();
        if (ports.isEmpty()) {
            ports.add(new Port(80, false));
            ports.add(new Port(443, true));
        }

        this.resolver.add(ServerSettings.class, this.settings);
        this.resolver.addFactory(ServerProperties.class, r -> ServerProperties.load());
        this.resolver.add(SSLServerSocketFactory.class, this.socketFactory);
        this.resolver.implement(ListenerFactory.class, this.listenerFactory);

        this.resolver.implementDefault(TemplatingService.class, DummyTemplatingService.class);
        this.resolver.implement(ExceptionHandler.class, DefaultExceptionHandler.class);
        this.resolver.implementDefault(MimeParser.class, FastMimeParser.class);

        this.resolver.addDefaultFactory(MimeDb.class, r -> {
            final MimeDb db = new MimeDb();
            final MimeDbLoader loader = r.get(MimeDbLoader.class);
            loader.load(this.getClass().getResourceAsStream("/net/cmpsb/cacofony/mime.types"),
                    db::register);
            return db;
        });

        return new Server(this.resolver, this.settings);
    }
}
