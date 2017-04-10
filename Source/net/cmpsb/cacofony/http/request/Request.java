package net.cmpsb.cacofony.http.request;

import net.cmpsb.cacofony.mime.MimeType;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * A basic HTTP request.
 *
 * @author Luc Everse
 */
public abstract class Request {
    /**
     * Returns the client's major HTTP version.
     *
     * @return the client's major HTTP version
     */
    public abstract int getMajorVersion();

    /**
     * Returns the client's minor HTTP version.
     *
     * @return the client's minor HTTP version
     */
    public abstract int getMinorVersion();

    /**
     * Returns the HTTP method (verb) used for this request.
     * <p>
     * This may not be the actual method requested. For instance, a GET route may be issued
     * if there is no explicit HEAD route. Use {@link #getRealMethod()} to get the method as sent
     * by the client.
     *
     * @return the HTTP method for this request
     */
    public abstract Method getMethod();

    /**
     * Returns the actual HTTP method (verb) used for this request.
     *
     * @return the HTTP method for this request
     */
    public abstract Method getRealMethod();

    /**
     * Returns the path given in the request line.
     * <p>
     * This is the raw, unprocessed path. Other methods may be of better use:
     * <table summary="Request path method features" border="1">
     *   <tr>
     *       <th>Method</th>
     *       <th>Scheme</th>
     *       <th>Host</th>
     *       <th>Path</th>
     *       <th>Query string</th>
     *   </tr>
     *   <tr>
     *       <td>{@link #getRawPath()}</td>
     *       <td></td>
     *       <td></td>
     *       <td>escaped</td>
     *       <td>escaped</td>
     *   </tr>
     *   <tr>
     *       <td>{@link #getUri()}</td>
     *       <td></td>
     *       <td></td>
     *       <td>unescaped</td>
     *       <td></td>
     *   </tr>
     *   <tr>
     *       <td>{@link #getFullUri()}</td>
     *       <td></td>
     *       <td></td>
     *       <td>unescaped</td>
     *       <td>unescaped</td>
     *   </tr>
     *   <tr>
     *       <td>{@link #getUrl()}</td>
     *       <td>yes</td>
     *       <td>yes</td>
     *       <td>unescaped</td>
     *       <td></td>
     *   </tr>
     *   <tr>
     *       <td>{@link #getFullUrl()}</td>
     *       <td>yes</td>
     *       <td>yes</td>
     *       <td>unescaped</td>
     *       <td>unescaped</td>
     *   </tr>
     * </table>
     *
     * @return the raw request path
     */
    public abstract String getRawPath();

    /**
     * Returns the processed request path.
     * <p>
     * This is the unescaped path without the host and query string.
     *
     * @return the processed request path
     */
    public abstract String getUri();

    /**
     * Returns the processed request path including the query string.
     * <p>
     * This is the unescaped path <strong>with</strong> the query string, but
     * <strong>without</strong> the scheme or host.
     *
     * @return the processed request path with query string
     */
    public abstract String getFullUri();

    /**
     * Returns the processed request path including the scheme and host.
     * <p>
     * This is the unescaped path <strong>with</strong> the scheme and host, but
     * <strong>without</strong> the query string.
     *
     * @return the processed request path with scheme and host
     */
    public abstract String getUrl();

    /**
     * Returns the processed request path including the query string, scheme and host.
     * <p>
     * This is the unescaped path <strong>with</strong> the query string, scheme and host.
     *
     * @return the processed request path with query string, scheme and host
     */
    public abstract String getFullUrl();

    /**
     * Returns the hostname as which the server is replying.
     * <p>
     * This value is based on the <code>Host</code> header.
     *
     * @return the current host
     */
    public abstract String getHost();

    /**
     * Returns the URL scheme used for this request.
     * <p>
     * This value may not be the actual scheme used; for requests over anything lower than HTTP/2
     * the scheme is inferred through the use of encryption: encrypted requests return
     * {@code https}, while plain text requests return {@code http}.
     *
     * @return the URL scheme
     */
    public abstract String getScheme();

    /**
     * Returns a parameter parsed from the URI.
     *
     * @param param the parameter to look for
     *
     * @return the value for that parameter or {@code null} if it doesn't exist
     */
    public abstract String getPathParameter(String param);

    /**
     * Returns a parameter parsed from the URI.
     *
     * @param param the parameter to look for
     * @param def   the default to return if the paramater doesn't exist
     *
     * @return the value for that parameter or {@code def} if it doesn't exist
     */
    public abstract String getPathParameter(String param, String def);

    /**
     * Returns all headers with all values that were sent in the original request.
     *
     * <p>
     * Each value is treated as an opaque block; no transformations are applied since some headers
     * have different semantics than others.
     *
     * <p>
     * Each entry in a header's list represents a header line in the original request. For example,
     * the header
     *
     * <pre>{@code Accept: text/html, application/xhtml+xml}</pre>
     *
     * will generate a single entry <code>"text/html, application/xhtml+xml"</code>, while the
     * headers
     *
     * <pre>{@code Accept: text/html, application/xhtml+xml
     * Accept: image/*}</pre>
     *
     * will generate two entries (in that order) <code>"text/html, application/xhtml+xml"</code>
     * and <code>"image/*"</code>.
     *
     * @return all headers in the request
     */
    public abstract Map<String, List<String>> getHeaders();

    /**
     * Gets all values for a single header.
     * <p>
     * This function performs a simple key lookup on the map returned by {@link #getHeaders()}.
     *
     * @param key the the name of the header to look for
     *
     * @return all values or null if the header wasn't included in the request
     */
    public abstract List<String> getHeaders(String key);

    /**
     * Gets the first value for a single header.
     * <p>
     * This function returns (if possible) the first entry in the list returned by
     * {@link #getHeaders(String)}.
     *
     * @param key the name of the header to look for
     *
     * @return the first received value or null if the header wasn't included in the request
     */
    public abstract String getHeader(String key);

    /**
     * Gets the first value for a single header or a default.
     * <p>
     * This function returns (if possible) the first entry in the list returned by
     * {@link #getHeaders(String)}.
     * <p>
     * If the header is missing then the value of {@code def} is returned instead.
     *
     * @param key the name of the header to look for
     * @param def the default value to return if the header is missing
     *
     * @return the first received value or {@code def} if the header wasn't included in the request
     */
    public String getHeader(final String key, final String def) {
        final String header = this.getHeader(key);

        if (header == null) {
            return def;
        }

        return header;
    }

    /**
     * Checks whether the request contains a certain header.
     *
     * @param key the header's name
     *
     * @return true if there are values for that header, false otherwise
     */
    public abstract boolean hasHeader(String key);

    /**
     * Returns the effective MIME type acceptable for this request.
     *
     * @return the effective MIME type acceptable for this request
     */
    public abstract MimeType getContentType();

    /**
     * Returns the body of the message as an input stream.
     * <p>
     * This stream may be filtered by zero or more transforming input streams because of
     * transfer encodings. It is not possible to access the raw incoming stream.
     *
     * @return the body as an input stream
     */
    public abstract InputStream getBody();

    /**
     * Returns the number of bytes in the request body.
     * <p>
     * A value of {@code -1} indicates an unknown length, possibly due to a request with a specific
     * transfer encoding.
     *
     * @return the number of bytes in the request or {@code -1}
     */
    public abstract long getContentLength();
}
