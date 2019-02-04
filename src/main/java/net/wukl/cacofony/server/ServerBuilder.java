package net.wukl.cacofony.server;

import net.wukl.cacodi.Manual;
import net.wukl.cacofony.exception.DefaultExceptionHandler;
import net.wukl.cacofony.exception.ExceptionHandler;
import net.wukl.cacofony.mime.FastMimeParser;
import net.wukl.cacofony.mime.MimeDb;
import net.wukl.cacofony.mime.MimeDbLoader;
import net.wukl.cacofony.mime.MimeParser;
import net.wukl.cacofony.templating.DummyTemplatingService;
import net.wukl.cacofony.templating.TemplatingService;
import net.wukl.cacodi.DependencyResolver;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import java.security.NoSuchAlgorithmException;
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
    @Manual
    public ServerBuilder() {
        this(new DependencyResolver());
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

        try {
            this.resolver.addDefault(SSLContext.class, SSLContext.getDefault());
        } catch (final NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }

        this.resolver.addDefaultFactory(SSLServerSocketFactory.class, r -> {
            final var context = r.get(SSLContext.class);
            return context.getServerSocketFactory();
        });

        this.resolver.addDefault(ServerSettings.class, this.settings);
        this.resolver.addDefaultFactory(ServerProperties.class, r -> ServerProperties.load());
        this.resolver.implementDefault(ListenerFactory.class, this.listenerFactory);

        this.resolver.implementDefault(TemplatingService.class, DummyTemplatingService.class);
        this.resolver.implementDefault(ExceptionHandler.class, DefaultExceptionHandler.class);
        this.resolver.implementDefault(MimeParser.class, FastMimeParser.class);

        this.resolver.addDefaultFactory(MimeDb.class, r -> {
            final MimeDb db = new MimeDb();
            final MimeDbLoader loader = r.get(MimeDbLoader.class);
            loader.load(this.getClass().getResourceAsStream("/net/wukl/cacofony/mime.types"),
                    db::register);
            return db;
        });

        return new Server(this.resolver, this.settings);
    }
}
