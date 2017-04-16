package net.cmpsb.cacofony.server;

import freemarker.template.Configuration;
import net.cmpsb.cacofony.templating.TemplatingService;
import net.cmpsb.cacofony.templating.freemarker.FreeMarkerTemplatingService;
import net.cmpsb.cacofony.util.Ob;

/**
 * A helper class with many useful utilities for preparing a server.
 *
 * @author Luc Everse
 */
public class ServerHelper {
    /**
     * The server to help.
     */
    private final Server server;

    /**
     * Creates a new server helper for a server.
     *
     * @param server the server to help
     */
    public ServerHelper(final Server server) {
        this.server = server;
    }

    /**
     * Enables FreeMarker for the server using a configuration.
     *
     * @param config the FreeMarker configuration to install
     */
    public void enableFreeMarker(final Configuration config) {
        final FreeMarkerTemplatingService service = this.server.getResolver().get(Ob.map(
                "configuration", config
        ), FreeMarkerTemplatingService.class);

        this.server.register(TemplatingService.class, service);
    }
}