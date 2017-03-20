package net.cmpsb.cacofony.http;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A basic HTTP request.
 *
 * @author Luc Everse
 */
public class Request {
    /**
     * The request method.
     */
    private final Method method;

    /**
     * The raw (unescaped, un-parsed) request path.
     */
    private final String rawPath;

    /**
     * The major HTTP version requested.
     */
    private final int versionMajor;

    /**
     * The minor HTTP version requested.
     */
    private final int versionMinor;

    /**
     * The request headers.
     */
    private final Map<String, List<String>> headers;

    /**
     * Create a new request.
     *
     * @param method       the request method
     * @param path         the request path
     * @param versionMajor the request version's major component
     * @param versionMinor the request version's minor component
     */
    public Request(final Method method, final String path,
                   final int versionMajor, final int versionMinor) {
        this.method = method;
        this.rawPath = path;
        this.versionMajor = versionMajor;
        this.versionMinor = versionMinor;

        this.headers = new HashMap<>();
    }

    /**
     * @return all headers in the request
     */
    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }

    /**
     * Get all values for a single header.
     *
     * @param key the the name of the header to look for
     *
     * @return all values or null if the header wasn't included in the request
     */
    public List<String> getHeaders(final String key) {
        return this.headers.get(key);
    }

    /**
     * Get the first value for a single header.
     *
     * @param key the name of the header to look for
     *
     * @return the first received value or null if the header wasn't included in the request
     */
    public String getHeader(final String key) {
        final List<String> values = this.headers.get(key);

        if (values == null) {
            return null;
        }

        return values.get(0);
    }

    /**
     * Check whether the request contains a certain header.
     *
     * @param key the header's name
     *
     * @return true if there are values for that header, false otherwise
     */
    public boolean hasHeader(final String key) {
        return this.headers.containsKey(key);
    }
}
