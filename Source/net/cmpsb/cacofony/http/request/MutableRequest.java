package net.cmpsb.cacofony.http.request;

import net.cmpsb.cacofony.http.Method;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A mutable HTTP request.
 * <p>
 * For correctness reasons you shouldn't use this class outside of testing.
 *
 * @author Luc Everse
 */
public class MutableRequest implements Request {
    /**
     * The request method.
     */
    private Method method = null;

    /**
     * The raw (unescaped, un-parsed) request path.
     */
    private String rawPath = null;

    /**
     * The major HTTP version requested.
     */
    private int versionMajor = -1;

    /**
     * The minor HTTP version requested.
     */
    private int versionMinor = -1;

    /**
     * The message body as an input stream.
     */
    private InputStream body;

    /**
     * The request headers.
     */
    private Map<String, List<String>> headers = new HashMap<>();

    /**
     * Creates a new request.
     *
     * @param method       the request method
     * @param path         the request path
     * @param versionMajor the request version's major component
     * @param versionMinor the request version's minor component
     */
    public MutableRequest(final Method method,
                          final String path,
                          final int versionMajor,
                          final int versionMinor) {
        this.method = method;
        this.rawPath = path;
        this.versionMajor = versionMajor;
        this.versionMinor = versionMinor;
    }

    /**
     * Creates a new, empty request.
     * <p>
     * Some values are invalid, set them before use.
     */
    public MutableRequest() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Method getMethod() {
        return this.method;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRawPath() {
        return this.rawPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUri() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFullUri() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFullUrl() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHost() {
        return this.getHeader("Host");
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getHeaders(final String key) {
        return this.headers.get(key);
    }

    /**
     * {@inheritDoc}
     */
    public String getHeader(final String key) {
        final List<String> values = this.headers.get(key);

        if (values == null) {
            return null;
        }

        return values.get(0);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasHeader(final String key) {
        return this.headers.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getBody() {
        return this.body;
    }

    /**
     * Set the request's body stream.
     *
     * @param body the input stream
     */
    public void setBody(final InputStream body) {
        this.body = body;
    }
}
