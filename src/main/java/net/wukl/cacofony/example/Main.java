package net.wukl.cacofony.example;

import freemarker.template.Configuration;
import net.wukl.cacofony.server.MutableServerSettings;
import net.wukl.cacofony.server.Server;
import net.wukl.cacofony.server.ServerBuilder;
import net.wukl.cacofony.server.host.HostBuilder;

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
        final MutableServerSettings settings = new MutableServerSettings();
        settings.addInsecurePort(8080);

        final ServerBuilder serverBuilder = new ServerBuilder();
        serverBuilder.setSettings(settings);

        final Server server = serverBuilder.build();
        final HostBuilder localhostBuilder = server.addHost("localhost");
        localhostBuilder.addControllers("net.cmpsb.cacofony.example.localhost");

        final HostBuilder loopbackBuilder = server.addHost("127.0.0.1");
        loopbackBuilder.addControllers("net.cmpsb.cacofony.example.loopback");

        final Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
        cfg.setClassForTemplateLoading(Main.class, "/net/wukl/cacofony/example");
        localhostBuilder.withFreeMarker(cfg);

        server.run();
    }
}
