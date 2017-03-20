package net.cmpsb.cacofony.http;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A parameter bag containing the parameters passed through more traditional ways:
 * the query string and the request body.
 *
 * @author Luc Everse
 */
public class HttpParameterBag implements ParameterBag {
    /**
     * The raw request to pull the parameters from.
     */
    private final HttpServletRequest rawRequest;

    /**
     * Create a new HTTP parameter bag.
     *
     * @param rawRequest the request to pull the parameters from
     */
    public HttpParameterBag(final HttpServletRequest rawRequest) {
        this.rawRequest = rawRequest;
    }

    /**
     * Get a parameter from the parameter bag.
     *
     * @param param the name of the parameter to look for
     * @param def   the default value in case the parameter couldn't be found
     *
     * @return the value of the parameter or the default if it couldn't be found
     */
    @Override
    public String get(final String param, final String def) {
        final String value = rawRequest.getParameter(param);

        if (value == null || value.isEmpty()) {
            return def;
        }

        return value;
    }

    /**
     * Get a parameter as a long integer.
     *
     * @param param the name of the parameter to look for
     * @param def   the default value in case the parameter couldn't be found
     *
     * @return the value of the parameter or the default if it couldn't be found
     */
    @Override
    public long get(final String param, final long def) {
        final String value = rawRequest.getParameter(param);

        if (value == null || value.isEmpty()) {
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
        final String[] bareList = rawRequest.getParameterValues(param);

        if (bareList == null) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(bareList);
    }

    /**
     * Check whether a parameter exists.
     *
     * @param param the name of the parameter to check
     * @return true if the parameter exists in this bag, false otherwise
     */
    @Override
    public boolean has(final String param) {
        return rawRequest.getParameterMap().containsKey(param);
    }
}
