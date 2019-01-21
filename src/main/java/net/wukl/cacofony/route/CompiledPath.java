package net.wukl.cacofony.route;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A compiled routing path.
 *
 * @author Luc Everse
 */
public class CompiledPath {
    /**
     * The routing path the entry represents.
     */
    private final String path;

    /**
     * The compiled regex pattern derived from the path.
     */
    private final Pattern pattern;

    /**
     * The names of the parameters this route accepts.
     */
    private final List<String> parameters;

    /**
     * A compiled routing path.
     *
     * @param path       the source path
     * @param pattern    the path compiled as a regex
     * @param parameters the names of the  parameters contained in the path
     */
    public CompiledPath(final String path,
                        final Pattern pattern,
                        final List<String> parameters) {
        this.path = path;
        this.pattern = pattern;
        this.parameters = parameters;
    }

    /**
     * @return the source path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * @return the path compiled as a regex
     */
    public Pattern getPattern() {
        return this.pattern;
    }

    /**
     * @return the names of the parameters contained in the path
     */
    public List<String> getParameters() {
        return this.parameters;
    }

    /**
     * Parses a matching pattern into a set of parameters.
     *
     * @param matcher the matching pattern
     *
     * @return a set of extracted parameters
     */
    public Map<String, String> parseParameters(final Matcher matcher) {
        final Map<String, String> params = new HashMap<>();

        for (final String key : this.parameters) {
            final String rawVal = matcher.group(key);
            final String val = this.decodeUriComponent(rawVal);

            params.put(key, val);
        }

        return params;
    }

    /**
     * Decodes a URI part from the RFC encoding.
     *
     * @param str the string to decode
     *
     * @return a decoded string
     */
    private String decodeUriComponent(final String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (final UnsupportedEncodingException ex) {
            // Not gonna happen.
            throw new RuntimeException(ex);
        }
    }

    /**
     * Check whether this object is equal to another.
     *
     * @param obj the other object
     *
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public final boolean equals(final Object obj) {
        if (!(obj instanceof CompiledPath)) {
            return false;
        }

        final CompiledPath otherPath = (CompiledPath) obj;

        return Objects.equals(this.path, otherPath.path)
            && Objects.equals(this.pattern, otherPath.pattern)
            && Objects.equals(this.parameters, otherPath.parameters);
    }

    /**
     * Calculate the object's hash code.
     *
     * @return the hash code
     */
    @Override
    public final int hashCode() {
        return Objects.hash(this.path, this.pattern, this.parameters);
    }
}
