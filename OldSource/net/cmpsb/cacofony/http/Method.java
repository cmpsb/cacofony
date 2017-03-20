package net.cmpsb.cacofony.http;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP method verbs.
 *
 * @author Luc Everse
 */
public enum Method {
    /**
     * A request for information about the communication options available.
     */
    OPTIONS,

    /**
     * A request for whatever information is represented by the request URI.
     */
    GET,

    /**
     * A request identical to GET, except that the server must not return a message body.
     */
    HEAD,

    /**
     * A request that the server store the enclosed entity under the request URI.
     */
    POST,

    /**
     * A request that the server updates the entity referred to by the request URI.
     */
    PUT,

    /**
     * A request that the server delete the entity referred to by the request URI.
     */
    DELETE,

    /**
     * A request used to invoke a loopback of the request message.
     */
    TRACE,

    /**
     * A request to partially update an existing entity.
     */
    PATCH,

    /**
     * Reserved.
     */
    CONNECT;

    /**
     * A mapping between the constants and their string forms.
     */
    private static final Map<String, Method> BY_NAME = new HashMap<>();

    static {
        // Populate the mapping.
        for (final Method method : values()) {
            BY_NAME.put(method.name(), method);
        }
    }

    /**
     * Look a value up by its string form.
     *
     * @param name the string form
     *
     * @return the method named by the input string
     *
     * @throws IllegalArgumentException if there is no method with that name
     */
    public static Method get(final String name) throws IllegalArgumentException {
        final Method method = BY_NAME.get(name);

        if (method == null) {
            throw new IllegalArgumentException("There is no method named \"" + name + "\"");
        }

        return method;
    }
}
