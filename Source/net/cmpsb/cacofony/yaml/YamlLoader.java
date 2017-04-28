package net.cmpsb.cacofony.yaml;

import net.cmpsb.cacofony.di.DependencyResolver;
import net.cmpsb.cacofony.di.Factory;
import net.cmpsb.cacofony.server.MutableServerSettings;
import net.cmpsb.cacofony.server.Server;
import net.cmpsb.cacofony.server.ServerBuilder;
import net.cmpsb.cacofony.server.host.HostBuilder;
import net.cmpsb.cacofony.templating.TemplatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * A loader that translates a yaml specification file (spec) to a server with hosts.
 *
 * @author Luc Everse
 */
public class YamlLoader {
    private static final Logger logger = LoggerFactory.getLogger(YamlLoader.class);

    /**
     * The yaml instance to use.
     */
    private final Yaml yaml;

    /**
     * The settings loader to use.
     */
    private final SettingsLoader settingsLoader;

    /**
     * Creates a new yaml loader.
     */
    public YamlLoader() {
        this.yaml = new Yaml();
        this.settingsLoader = new SettingsLoader();
    }

    /**
     * Builds a server based on a yaml spec.
     *
     * @param file the file the spec is in
     *
     * @return a server
     */
    @SuppressWarnings("unchecked")
    public Server load(final InputStream file) {
        final Map<String, Object> config = (Map<String, Object>) this.yaml.load(file);

        final DependencyResolver resolver = new DependencyResolver();
        final ServerBuilder builder = new ServerBuilder(resolver);

        // Load the settings.
        final MutableServerSettings masterSettings = this.settingsLoader.load(config);
        builder.setSettings(masterSettings);

        final Server server = builder.build();

        final Map<String, Map<String, Object>> hosts =
                (Map<String, Map<String, Object>>) config.get("hosts");

        if (hosts == null) {
            throw new InvalidYamlException("No hosts specified.");
        }

        for (final String hostname : hosts.keySet()) {
            logger.debug("Found host {}.", hostname);

            final Map<String, Object> spec = hosts.get(hostname);

            if ((boolean) spec.getOrDefault("default", false)) {
                this.loadHost(server.addDefaultHost(hostname), spec);
            } else {
                this.loadHost(server.addHost(hostname), spec);
            }
        }

        return server;
    }

    /**
     * Builds a host based on the yaml spec.
     *
     * @param builder the host builder
     * @param spec    the yaml spec
     */
    @SuppressWarnings("unchecked")
    private void loadHost(final HostBuilder builder, final Map<String, Object> spec) {
        final List<Object> controllers = (List<Object>) spec.get("controllers");
        this.addControllers(builder, controllers);

        final Map<String, Object> templating = (Map<String, Object>) spec.get("templating");
        this.addTemplating(builder, templating);

        final List<Map> resources = (List<Map>) spec.get("resources");
        this.addStaticResources(builder, resources);
    }

    /**
     * Adds the controllers from the yaml spec to the host.
     *
     * @param builder     the host builder
     * @param controllers the controllers
     */
    @SuppressWarnings("unchecked")
    private void addControllers(final HostBuilder builder, final List<Object> controllers) {
        if (controllers == null) {
            return;
        }

        for (final Object controllerSpec : controllers) {
            if (controllerSpec instanceof String) {
                // Take the string as the package name.
                builder.addControllers((String) controllerSpec);
            } else if (controllerSpec instanceof Map) {
                final Map<String, String> spec = (Map<String, String>) controllerSpec;

                final String pack = spec.get("package");
                final String prefix = spec.get("prefix");

                builder.addControllers(pack, prefix);
            }
        }
    }

    /**
     * Adds a templating override to the host.
     *
     * @param builder the host builder
     * @param spec    the yaml spec
     */
    @SuppressWarnings("unchecked")
    private void addTemplating(final HostBuilder builder, final Map<String, Object> spec) {
        if (spec == null) {
            return;
        }

        final String factoryClass = (String) spec.get("factory");
        if (factoryClass == null) {
            throw new InvalidYamlException("No templating service factory factory specified.");
        }

        final FactoryFactory<TemplatingService> factory;
        factory = (FactoryFactory<TemplatingService>) this.getInstance(factoryClass);

        final Map<String, Object> options = (Map<String, Object>) spec.get("options");
        final Factory<TemplatingService> fac = factory.build(options);
        builder.setTemplatingServiceFactory(fac);
    }

    /**
     * Reads all static resource specs from the file.
     *
     * @param builder the builder
     * @param spec    the yaml spec
     */
    @SuppressWarnings("unchecked")
    private void addStaticResources(final HostBuilder builder, final List<Map> spec) {
        if (spec == null) {
            return;
        }

        for (final Map map : spec) {
            this.addStaticResource(builder, map);
        }
    }

    /**
     * Reads a static resource spec from the file and adds it to the selected host.
     *
     * @param builder the host builder
     * @param spec    the yaml spec
     */
    private void addStaticResource(final HostBuilder builder, final Map<String, String> spec) {
        if (spec == null) {
            return;
        }

        final String prefix = spec.get("prefix");
        final String path = spec.get("path");

        if (prefix == null || path == null) {
            throw new InvalidYamlException(
                "Both a prefix and path need to be set for static files or resources."
            );
        }

        final String className = spec.get("class");
        if (className != null) {
            final Class<?> type = this.getClass(className);
            builder.addStaticResources(prefix, type, path);
        } else {
            builder.addStaticFiles(prefix, path);
        }
    }

    /**
     * Gets the class with that name.
     *
     * @param name the name of the class
     *
     * @return the class with that name
     *
     * @throws InvalidYamlException if the name cannot be resolved
     */
    private Class<?> getClass(final String name) {
        try {
            return Class.forName(name);
        } catch (final ClassNotFoundException ex) {
            throw new InvalidYamlException("Class " + name + " not found: ", ex);
        }
    }

    /**
     * Gets an instance of the class with that name.
     * <p>
     * This does not call the dependency resolver. Only nullary constructors are supported.
     *
     * @param name the name of the class to instantiate
     *
     * @return the instance
     */
    private Object getInstance(final String name) {
        try {
            return this.getClass(name).newInstance();
        } catch (final IllegalAccessException | InstantiationException ex) {
            throw new RuntimeException(ex);
        }
    }
}
