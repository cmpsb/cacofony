package net.wukl.cacofony.example;

import net.wukl.cacodi.DependencyResolver;
import net.wukl.cacofony.yaml.YamlLoader;

import java.io.IOException;

/**
 * The entry point to the example server.
 *
 * @author Luc Everse
 */
public final class Main {
    /**
     * Do not instantiate.
     */
    private Main() {
        throw new AssertionError("Do not instantiate.");
    }

    /**
     * Entry point for the example server.
     *
     * @param args any command-line arguments
     *
     * @throws IOException if an I/O error occurs
     */
    public static void main(final String[] args) throws IOException {
        final var resolver = new DependencyResolver();
        final var yamlLoader = resolver.get(YamlLoader.class);

        final var server = yamlLoader.load(
                Main.class.getResourceAsStream("/net/wukl/cacofony/example/server.yml")
        );
        server.run();
    }
}
