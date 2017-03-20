package net.cmpsb.cacofony.http;

import java.util.List;

/**
 * A collection of parameters.
 *
 * @author Luc Everse
 */
public interface ParameterBag {
    /**
     * Get a parameter from the parameter bag.
     *
     * @param param the name of the parameter to look for
     * @param def   the default value in case the parameter couldn't be found
     *
     * @return the value of the parameter or the default if it couldn't be found
     */
    String get(String param, String def);

    /**
     * Get a parameter as a long integer.
     *
     * @param param the name of the parameter to look for
     * @param def   the default value in case the parameter couldn't be found
     *
     * @return the value of the parameter or the default if it couldn't be found
     */
    long get(String param, long def);

    /**
     * Get all values associated with this parameter name.
     *
     * The returned list may be any size; even if the parameter doesn't exist this method
     * will still return a 0-length list.
     *
     * The list is immutable.
     *
     * @param param the name of the parameter to look for
     *
     * @return a list of values associated with this parameter
     */
    List<String> getAll(String param);

    /**
     * Check whether a parameter exists.
     *
     * @param param the name of the parameter to check
     *
     * @return true if the parameter exists in this bag, false otherwise
     */
    boolean has(String param);
}
