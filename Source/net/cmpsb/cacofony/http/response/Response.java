package net.cmpsb.cacofony.http.response;

import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.mime.MimeType;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A base response object.
 *
 * @author Luc Everse
 */
public abstract class Response {
    /**
     * The HTTP return status code.
     */
    private ResponseCode status = ResponseCode.OK;

    /**
     * The content's MIME type.
     * Defaults to {@code application/octet-stream}.
     */
    private MimeType contentType = null;

    /**
     * The response's headers.
     */
    private final Map<String, List<String>> headers = new HashMap<>();

    /**
     * Whether to allow compression on the set response. If {@code null}, the server will decide
     * based on the global settings.
     */
    private Boolean compressionAllowed = null;

    /**
     * Creates a new, empty response.
     * <p>
     * The HTTP response code is set to 200 OK.
     */
    public Response() {
    }

    /**
     * Creates an empty response with a HTTP response code.
     *
     * @param status the HTTP response code
     */
    public Response(final ResponseCode status) {
        this.status = status;
    }

    /**
     * @return the HTTP response code
     */
    public ResponseCode getStatus() {
        return this.status;
    }

    /**
     * Sets the HTTP status return code.
     *
     * @param code the status code
     */
    public void setStatus(final ResponseCode code) {
        this.status = code;
    }

    /**
     * Sets the response's content type.
     *
     * @param type the content type
     */
    public void setContentType(final MimeType type) {
        this.contentType = type;
    }

    /**
     * @return the response's content type
     */
    public MimeType getContentType() {
        return this.contentType;
    }

    /**
     * Adds a header value to the response.
     *
     * @param key   the header's name
     * @param value the header's value
     */
    public void addHeader(final String key, final String value) {
        this.headers.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    /**
     * Adds multiple header values to the response.
     *
     * @param key    the header's name
     * @param values the header's values
     */
    public void addHeader(final String key, final List<String> values) {
        this.headers.computeIfAbsent(key, k -> new ArrayList<>()).addAll(values);
    }

    /**
     * Sets a header to a single value, removing all previous values if there were any.
     *
     * @param key   the header's name
     * @param value the header's value
     */
    public void setHeader(final String key, final String value) {
        this.headers.put(key, Collections.singletonList(value));
    }

    /**
     * Returns the response's headers.
     *
     * @return the response's headers
     */
    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }

    /**
     * Adopts the headers from another map.
     *
     *
     * @param otherHeaders the other headers
     */
    public void adoptHeaders(final Map<String, List<String>> otherHeaders) {
        for (final String key : otherHeaders.keySet()) {
            this.addHeader(key, otherHeaders.get(key));
        }
    }

    /**
     * Returns whether compression is allowed for this response.
     * <p>
     * <ol>
     *     <li>{@code true}: the server will attempt to apply compression where possible</li>
     *     <li>{@code false}: the server will not compress the data (though it may chunk it)</li>
     *     <li>{@code null}: the server will decide whether to compress the data by looking at
     *     {@link net.cmpsb.cacofony.server.ServerSettings#canCompressByDefault()}</li>
     * </ol>
     * <p>
     * If {@link net.cmpsb.cacofony.server.ServerSettings#isCompressionEnabled()} is {@code false},
     * the server will never compress any response, regardless of this value.
     *
     * @return {@code true} to compress the response, {@code false} to not to,
     * {@code null} to let the server decide
     */
    public Boolean isCompressionAllowed() {
        return this.compressionAllowed;
    }

    /**
     * Returns whether compression is allowed for this response with the current server settings.
     *
     * @param isAllowedServerWide whether compression is allowed on the entire server
     *
     * @return true if compression is allowed, false otherwise
     */
    public boolean isCompressionAllowed(final boolean isAllowedServerWide) {
        if (this.compressionAllowed == null) {
            return isAllowedServerWide;
        }

        return this.compressionAllowed;
    }

    /**
     * Sets whether the response allows compression.
     *
     * @param compressionAllowed true to allow compression, false to disable it,
     *                           null to let the server decide
     */
    public void setCompressionAllowed(final Boolean compressionAllowed) {
        this.compressionAllowed = compressionAllowed;
    }

    /**
     * Writes the response body to the output stream.
     *
     * @param out the client's output stream
     *
     * @throws IOException if an I/O error occurs during writing
     */
    public abstract void write(OutputStream out) throws IOException;

    /**
     * Calculates the length, in bytes, of the data to send.
     * <p>
     * If {@code -1}, then a collection of transfer encodings are applied. This allows for
     * big responses that don't fit entirely within memory.
     *
     * @return the length, in bytes, of the data to send or {@code -1} if it's unknown
     */
    public abstract long getContentLength();

    /**
     * Prepares the response for transmission.
     *
     * @param request the request that triggered this response
     */
    public void prepare(final Request request) {
    }
}
