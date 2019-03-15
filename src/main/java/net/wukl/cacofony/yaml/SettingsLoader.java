package net.wukl.cacofony.yaml;

import net.wukl.cacofony.http.encoding.TransferEncoding;
import net.wukl.cacofony.server.MutableServerSettings;
import net.wukl.cacofony.server.Port;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class for parsing server settings from a yaml specification.
 *
 * @author Luc Everse
 */
@SuppressWarnings("unchecked")
public class SettingsLoader {

    /**
     * Parses server settings from a yaml specification.
     *
     * @param spec the yaml spec
     *
     * @return the parsed server settings
     */
    public MutableServerSettings load(final Map<String, Object> spec) {
        final MutableServerSettings settings = new MutableServerSettings();

        final boolean compressionEnabled =
                this.get(spec, "compression enabled", settings.isCompressionEnabled());
        final boolean compressByDefault =
                this.get(spec, "compress by default", settings.canCompressByDefault());
        final boolean broadcastServerVersion =
                this.get(spec, "broadcast server version", settings.mayBroadcastServerVersion());
        final boolean http2Enabled = this.get(spec, "http/2", settings.isHttp2Enabled());

        settings.setCompressionEnabled(compressionEnabled);
        settings.setCompressByDefault(compressByDefault);
        settings.setBroadcastServerVersion(broadcastServerVersion);
        settings.setHttp2Enabled(http2Enabled);

        this.setPorts(settings, spec);
        this.setCompressionAlgorithms(settings, spec);

        return settings;
    }

    /**
     * Parses the ports from the yaml spec and adds them to the server settings.
     *
     * @param settings the server settings
     * @param spec     the yaml spec
     */
    private void setPorts(final MutableServerSettings settings,
                          final Map<String, Object> spec) {
        final List<Object> ports = (List<Object>) spec.get("ports");

        if (ports == null) {
            // Don't overidde the defaults if no values are set.
            return;
        }

        for (final Object portSpec : ports) {
            final Port port;

            if (portSpec instanceof Number) {
                // Take the port as a number and decide based on that whether it's secure.
                final int portNum = (int) portSpec;

                port = new Port(portNum, portNum == 80 || portNum == 8080);
            } else if (portSpec instanceof Map) {
                // Parse the object into an integer and an optional boolean.
                final Map<String, Object> subSpec = (Map<String, Object>) portSpec;

                final int portNum = (int) subSpec.get("port");
                final boolean secure = (boolean) subSpec.getOrDefault("secure", true);

                port = new Port(portNum, secure);
            } else {
                // Don't know what to do: error.
                throw new InvalidYamlException("Unknown port specification format.");
            }

            settings.addPort(port);
        }
    }

    /**
     * Sets the compression algorithms used by the server.
     *
     * @param settings the settings to store the set in
     * @param spec     the yaml spec
     */
    private void setCompressionAlgorithms(final MutableServerSettings settings,
                                          final Map<String, Object> spec) {
        final List<String> names = (List<String>) spec.get("algorithms");
        if (names == null) {
            // Don't override the defaults if the values are missing.
            return;
        }

        // Otherwise turn them into transfer encoding instances set the new values.
        final Set<TransferEncoding> algorithms = new HashSet<>();
        for (final String name : names) {
            final TransferEncoding algorithm = TransferEncoding.get(name);

            if (algorithm == null) {
                throw new InvalidYamlException("Unknown compression algorithm \"" + name + "\".");
            }

            algorithms.add(algorithm);
        }

        settings.setCompressionAlgorithms(algorithms);
    }

    /**
     * Reads a boolean from the spec.
     *
     * @param spec the spec
     * @param key  the key inside the spec
     * @param def  the default value if the key is missing
     *
     * @return the parsed value or {@code def} if the key is missing
     */
    private boolean get(final Map<String, Object> spec, final String key, final boolean def) {
        try {
            return (boolean) spec.getOrDefault(key, def);
        } catch (final ClassCastException ex) {
            throw new InvalidYamlException(
                    "Can't cast the value of setting \"" + key + "\" (\"" + spec.get(key)
                            + "\") as a boolean.",
                    ex
            );
        }
    }
}
