package net.cmpsb.cacofony.example;

import freemarker.template.Configuration;
import net.cmpsb.cacofony.server.Server;
import net.cmpsb.cacofony.templating.TemplatingService;
import net.cmpsb.cacofony.templating.freemarker.FreeMarkerTemplatingService;
import net.cmpsb.cacofony.util.Ob;

import java.io.IOException;

/**
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
        final Server server = new Server();
        server.addPort(8080);

        final FreeMarkerTemplatingService service = getFreeMarker(server);
        server.register(TemplatingService.class, service);

        server.scanPackage("net.cmpsb.cacofony.example");
        server.start();
    }

    /**
     * Creates a FreeMarker templating service.
     *
     * @param server the server the service is for
     *
     * @return a FreeMarker templating service
     */
    private static FreeMarkerTemplatingService getFreeMarker(final Server server) {
        if (!server.isReady()) {
            server.init();
        }

        final Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);

        cfg.setClassForTemplateLoading(Main.class, "/net/cmpsb/cacofony/example");

        return server.getResolver().get(Ob.map(
                "configuration", cfg
        ), FreeMarkerTemplatingService.class);
    }
}
