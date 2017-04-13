package net.cmpsb.cacofony.example;

import freemarker.template.Configuration;
import net.cmpsb.cacofony.server.MutableServerSettings;
import net.cmpsb.cacofony.server.Server;
import net.cmpsb.cacofony.server.ServerHelper;

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

        final Server server = new Server(settings);
        final ServerHelper helper = new ServerHelper(server);

        final Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
        cfg.setClassForTemplateLoading(Main.class, "/net/cmpsb/cacofony/example");
        helper.enableFreeMarker(cfg);

        server.scanPackage("net.cmpsb.cacofony.example");
        server.addStaticFiles("/static", "D:\\Dust\\Cacofony\\");
        server.start();
    }
}
