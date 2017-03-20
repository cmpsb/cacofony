package net.cmpsb.cacofony.http;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A parameter bag containing the parameters set via route paths.
 *
 * @author Luc Everse
 */
public class RouteParameterBag implements ParameterBag {
    /**
     * The map where to take parameters from.
     */
    private final Map<String, String> source;

    /**
     * Create a new parameter bag from a parameter mapping.
     *
     * @param source the map to read parameters from
     */
    public RouteParameterBag(final Map<String, String> source) {
        this.source = source;
    }

    /**
     * Get a parameter from the parameter bag.
     *
     * @param param the name of the parameter to look for
     * @param def   the default value in case the parameter couldn't be found
     * @return the value of the parameter or the default if it couldn't be found
     */
    @Override
    public String get(final String param, final String def) {
        if (!this.has(param)) {
            return def;
        }

        final String value = this.source.get(param);

        if (value.isEmpty()) {
            return def;
        }

        return value;
    }

    /**
     * Get a parameter as a long integer.
     *
     * @param param the name of the parameter to look for
     * @param def   the default value in case the parameter couldn't be found
     * @return the value of the parameter or the default if it couldn't be found
     */
    @Override
    public long get(final String param, final long def) {
        if (!this.has(param)) {
            return def;
        }

        final String value = this.source.get(param);

        if (value.isEmpty()) {
            return def;
        }

        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException ex) {
            return def;
        }
    }

    /**
     * Get all values associated with this parameter name.
     *
     * For this bag, the returned list will always be at most 1 items long as repeating the
     * same parameter name is not supported.
     *
     * The returned list may be any size; even if the parameter doesn't exist this method
     * will still return a 0-length list.
     *
     * The list is immutable.
     *
     * @param param the name of the parameter to look for
     * @return a list of values associated with this parameter
     */
    @Override
    public List<String> getAll(final String param) {
        final String value = this.get(param, null);

        if (value == null) {
            return Collections.EMPTY_LIST;
        }

        return Collections.singletonList(value);
    }

    /**
     * Check whether a parameter exists.
     *
     * @param param the name of the parameter to check
     * @return true if the parameter exists in this bag, false otherwise
     */
    @Override
    public boolean has(final String param) {
        return this.source.containsKey(param);
    }
}
