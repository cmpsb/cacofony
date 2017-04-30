package net.cmpsb.cacofony.http.request;

import net.cmpsb.cacofony.http.cookie.Cookie;
import net.cmpsb.cacofony.http.exception.BadRequestException;
import net.cmpsb.cacofony.mime.MimeType;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
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
public class MutableRequest extends Request {
    /**
     * The request method.
     */
    private Method method = null;

    /**
     * The real request method.
     */
    private Method realMethod = null;

    /**
     * The raw (unescaped, un-parsed) request path.
     */
    private String path = "";

    /**
     * The unescaped path.
     */
    private String unescapedPath = "";

    /**
     * The request's query string.
     */
    private String queryString = "";

    /**
     * The unescaped query string.
     */
    private String unescapedQueryString = "";

    /**
     * The request scheme.
     */
    private String scheme = "";

    /**
     * The request host.
     */
    private String host = null;

    /**
     * The request port.
     */
    private int port = 0;

    /**
     * The major HTTP version requested.
     */
    private int versionMajor = -1;

    /**
     * The minor HTTP version requested.
     */
    private int versionMinor = -1;

    /**
     * The number of bytes in the request body.
     */
    private long contentLength = -1;

    /**
     * The message body as an input stream.
     */
    private InputStream body;

    /**
     * The request headers.
     */
    private Map<String, List<String>> headers = new HashMap<>();

    /**
     * The parameters parsed from the request path.
     */
    private Map<String, String> pathParameters = new HashMap<>();

    /**
     * The query string parameters from the request path.
     */
    private Map<String, String> queryParameters = new HashMap<>();

    /**
     * The cookies in the request.
     */
    private Map<String, List<Cookie>> cookies = new HashMap<>();

    /**
     * The acceptable content type.
     */
    private MimeType contentType;

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
        this.realMethod = method;
        this.versionMajor = versionMajor;
        this.versionMinor = versionMinor;

        this.setPath(path, "");
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
    public int getMajorVersion() {
        return this.versionMajor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMinorVersion() {
        return this.versionMinor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Method getMethod() {
        return this.method;
    }

    /**
     * Sets the request method.
     *
     * @param method the method
     */
    public void setMethod(final Method method) {
        this.method = method;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Method getRealMethod() {
        return this.realMethod;
    }

    /**
     * Sets the real method.
     *
     * @param method the method
     */
    public void setRealMethod(final Method method) {
        this.realMethod = method;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRawPath() {
        return this.path + this.queryString;
    }

    /**
     * Sets the path.
     *
     * @param path the path
     * @param queryString the query string, including the leading question mark
     */
    public void setPath(final String path, final String queryString) {
        this.path = path;
        this.unescapedPath = this.decodeUriComponent(path);

        this.queryString = queryString;
        this.unescapedQueryString = this.decodeUriComponent(queryString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUri() {
        return this.path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFullUri() {
        return this.path + this.unescapedQueryString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return this.scheme + "://" + this.getHost() + this.unescapedPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFullUrl() {
        return this.getUrl() + this.unescapedQueryString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHost() {
        if (this.host == null) {
            final List<String> values = this.getHeaders("host");

            if (values == null || values.size() != 1) {
                throw new BadRequestException("None or multiple Host headers present.");
            }

            final String hostSpec = values.get(0);
            final int colonIndex = hostSpec.indexOf(':');

            if (colonIndex != -1) {
                this.host = hostSpec.substring(0, colonIndex);
            } else {
                this.host = hostSpec;
            }
        }

        return this.host;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPort() {
        return this.port;
    }

    /**
     * Sets the request port.
     *
     * @param port the port
     */
    public void setPort(final int port) {
       this.port = port;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getScheme() {
        return this.scheme;
    }

    /**
     * Returns whether a path parameter is present or not.
     *
     * @param param the name of the parameter to look for
     * @return {@code true} if the parameter is present, otherwise {@code false}
     */
    @Override
    public boolean hasPathParameter(final String param) {
        return false;
    }

    /**
     * Sets the request scheme.
     *
     * @param scheme the scheme
     */
    public void setScheme(final String scheme) {
        this.scheme = scheme;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPathParameter(final String param) {
        return this.pathParameters.get(param);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasQueryParameter(final String name) {
        return this.queryParameters.containsKey(name);
    }

    /**
     * Sets the path parameters.
     *
     * @param pathParameters the path parameters
     */
    public void setPathParameters(final Map<String, String> pathParameters) {
        this.pathParameters = pathParameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQueryParameter(final String param) {
        return this.queryParameters.get(param);
    }

    /**
     * Sets the query parameters.
     *
     * @param queryParameters the query parameters
     */
    public void setQueryParameters(final Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cookie getCookie(final String name) {
        final List<Cookie> filteredCookies = this.getCookies(name);

        if (filteredCookies == null) {
            return null;
        }

        return filteredCookies.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Cookie> getCookies(final String name) {
        final List<Cookie> filteredCookies = this.cookies.get(name.toLowerCase());

        if (filteredCookies == null) {
            return null;
        }

        return filteredCookies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<Cookie>> getCookies() {
        return this.cookies;
    }

    /**
     * Sets the request's cookies.
     *
     * @param cookies the cookies
     */
    public void setCookies(final Map<String, List<Cookie>> cookies) {
        this.cookies = cookies;
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
        return this.headers.get(key.toLowerCase());
    }

    /**
     * {@inheritDoc}
     */
    public String getHeader(final String key) {
        final List<String> values = this.headers.get(key.toLowerCase());

        if (values == null) {
            return null;
        }

        return values.get(0);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasHeader(final String key) {
        return this.headers.containsKey(key.toLowerCase());
    }

    /**
     * Adopts the headers from another map.
     *
     * @param otherHeaders the other headers
     */
    public void adoptHeaders(final Map<String, List<String>> otherHeaders) {
        for (final String key : otherHeaders.keySet()) {
            this.headers.computeIfAbsent(key, k -> new ArrayList<>()).addAll(otherHeaders.get(key));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MimeType getContentType() {
        return this.contentType;
    }

    /**
     * Sets the content type.
     *
     * @param type the content type
     */
    public void setContentType(final MimeType type) {
        this.contentType = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getBody() {
        return this.body;
    }

    /**
     * Sets the request's body stream.
     *
     * @param body the input stream
     */
    public void setBody(final InputStream body) {
        this.body = body;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getContentLength() {
        return this.contentLength;
    }

    /**
     * Sets the request's body length.
     *
     * @param contentLength the length in bytes
     */
    public void setContentLength(final long contentLength) {
        this.contentLength = contentLength;
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
}
