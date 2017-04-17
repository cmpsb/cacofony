package net.cmpsb.cacofony.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Static properties that are not configurable.
 * <p>
 * This includes the server version.
 *
 * @author Luc Everse
 */
public class ServerProperties extends Properties {
    private static final Logger logger = LoggerFactory.getLogger(ServerProperties.class);

    /**
     * The path to the resource holding the server's static properties.
     */
    private static final String METADATA_PATH = "/net/cmpsb/cacofony/metadata.xml";

    /**
     * Loads the server properties from the server's resources.
     *
     * @return the properties
     */
    public static ServerProperties load() {
        final ServerProperties properties = new ServerProperties();
        try (InputStream in = ServerProperties.class.getResourceAsStream(METADATA_PATH)) {
            properties.loadFromXML(in);
            logger.info("Cacofony v{}", properties.getProperty("net.cmpsb.cacofony.version"));
        } catch (final IOException ex) {
            logger.error("I/O exception while loading server metadata: ", ex);
        }

        return properties;
    }
}
