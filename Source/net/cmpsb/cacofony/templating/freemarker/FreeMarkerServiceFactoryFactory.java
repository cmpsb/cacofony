package net.cmpsb.cacofony.templating.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Version;
import net.cmpsb.cacofony.di.Factory;
import net.cmpsb.cacofony.mime.MimeGuesser;
import net.cmpsb.cacofony.mime.MimeParser;
import net.cmpsb.cacofony.yaml.FactoryFactory;
import net.cmpsb.cacofony.yaml.InvalidYamlException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Luc Everse
 */
public class FreeMarkerServiceFactoryFactory implements FactoryFactory<FreeMarkerService> {
    /**
     * The regex pattern used to extract the version.
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");

    /**
     * Builds the FreeMarker service factory.
     *
     * @param options the YML options
     *
     * @return a factory
     */
    @SuppressWarnings("unchecked")
    public Factory<FreeMarkerService> build(final Map<String, Object> options) {
        final Version version = this.getVersion((String) options.get("version"));
        final Configuration config = new Configuration(version);

        this.setLoader(config, (Map<String, String>) options.get("loader"));

        return resolver -> {
            final MimeParser parser = resolver.get(MimeParser.class);
            final MimeGuesser guesser = resolver.get(MimeGuesser.class);
            return new FreeMarkerService(config, parser, guesser);
        };
    }

    /**
     * Extracts the version from a string.
     *
     * @param versionStr the string containing the version or {@code null} to use the default
     *
     * @return the version
     */
    private Version getVersion(final String versionStr) {
        if (versionStr == null) {
             return Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS;
        }

        final Matcher versionMatcher = VERSION_PATTERN.matcher(versionStr);

        if (!versionMatcher.matches()) {
            throw new InvalidYamlException("FreeMarker does not match major.minor.version.");
        }

        final int major = Integer.parseInt(versionMatcher.group(1));
        final int minor = Integer.parseInt(versionMatcher.group(2));
        final int patch = Integer.parseInt(versionMatcher.group(3));

        return new Version(major, minor, patch);
    }

    /**
     * Sets the loader by examining the YML options.
     *
     * @param config the FreeMarker configuration
     * @param spec   the YML options
     */
    private void setLoader(final Configuration config, final Map<String, String> spec) {
        if (spec == null) {
            throw new InvalidYamlException("FreeMarker requires a loader.");
        }

        final String path = spec.get("path");
        if (path == null) {
            throw new InvalidYamlException("FreeMarker loaders require a path.");
        }

        final String className = spec.get("class");
        if (className != null) {
            try {
                final Class type = Class.forName(className);

                config.setClassForTemplateLoading(type, path);
            } catch (final ClassNotFoundException ex) {
                throw new InvalidYamlException("Template loader class not found: ", ex);
            }
        } else {
            try {
                config.setDirectoryForTemplateLoading(new File(path));
            } catch (final IOException ex) {
                throw new RuntimeException(
                    "I/O exception while adding directory template loader:",
                    ex
                );
            }
        }
    }
}
