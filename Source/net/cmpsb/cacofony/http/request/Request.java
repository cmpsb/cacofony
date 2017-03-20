package net.cmpsb.cacofony.http.request;

import net.cmpsb.cacofony.http.Method;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * A basic HTTP request.
 *
 * @author Luc Everse
 */
public interface Request {
    /**
     * Returns the HTTP method (verb) used for this request.
     *
     * @return the HTTP method for this request
     */
    Method getMethod();

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
    String getRawPath();

    /**
     * Returns the processed request path.
     * <p>
     * This is the unescaped path without the host and query string.
     *
     * @return the processed request path
     */
    String getUri();

    /**
     * Returns the processed request path including the query string.
     * <p>
     * This is the unescaped path <strong>with</strong> the query string, but
     * <strong>without</strong> the scheme or host.
     *
     * @return the processed request path with query string
     */
    String getFullUri();

    /**
     * Returns the processed request path including the scheme and host.
     * <p>
     * This is the unescaped path <strong>with</strong> the scheme and host, but
     * <strong>without</strong> the query string.
     *
     * @return the processed request path with scheme and host
     */
    String getUrl();

    /**
     * Returns the processed request path including the query string, scheme and host.
     * <p>
     * This is the unescaped path <strong>with</strong> the query string, scheme and host.
     *
     * @return the processed request path with query string, scheme and host
     */
    String getFullUrl();

    /**
     * Returns the hostname as which the server is replying.
     * <p>
     * This value is based on the <code>Host</code> header.
     *
     * @return the current host
     */
    String getHost();

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
    Map<String, List<String>> getHeaders();

    /**
     * Gets all values for a single header.
     * <p>
     * This function performs a simple key lookup on the map returned by {@link #getHeaders()}.
     *
     * @param key the the name of the header to look for
     *
     * @return all values or null if the header wasn't included in the request
     */
    List<String> getHeaders(String key);

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
    String getHeader(String key);

    /**
     * Checks whether the request contains a certain header.
     *
     * @param key the header's name
     *
     * @return true if there are values for that header, false otherwise
     */
    boolean hasHeader(String key);

    /**
     * Returns the body of the message as an input stream.
     * <p>
     * This stream may be filtered by zero or more transforming input streams because of
     * transfer encodings. It is not possible to access the raw incoming stream.
     *
     * @return the body as an input stream
     */
    InputStream getBody();
}
