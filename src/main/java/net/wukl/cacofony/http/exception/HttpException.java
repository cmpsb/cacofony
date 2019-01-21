package net.wukl.cacofony.http.exception;

import net.wukl.cacofony.http.response.ResponseCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An exception that has a meaning in the HTTP context.
 * These exceptions define a corresponding status code.
 *
 * @author Luc Everse
 */
public class HttpException extends RuntimeException {
    /**
     * The HTTP status code.
     */
    private final ResponseCode code;

    /**
     * Any headers to send with the response.
     */
    private final Map<String, List<String>> headers = new HashMap<>();

    /**
     * Create a new HTTP exception.
     *
     * @param code    the HTTP status code
     */
    public HttpException(final ResponseCode code) {
        this.code = code;
    }

    /**
     * Create a new HTTP exception.
     *
     * @param code    the HTTP status code
     * @param message the detail message
     */
    public HttpException(final ResponseCode code, final String message) {
        super(message);
        this.code = code;
    }

    /**
     * Create a new HTTP exception.
     *
     * @param code    the HTTP status code
     * @param cause   the exception that caused this exception
     */
    public HttpException(final ResponseCode code, final Throwable cause) {
        super(cause);
        this.code = code;
    }

    /**
     * Create a new HTTP exception.
     *
     * @param code    the HTTP status code
     * @param message the detail message
     * @param cause   the exception that caused this exception
     */
    public HttpException(final ResponseCode code, final String message, final Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * @return the HTTP status code this exception represents
     */
    public ResponseCode getCode() {
        return this.code;
    }

    /**
     * Adds a header value to the exception.
     *
     * @param key   the header's name
     * @param value the header's value
     */
    public void addHeader(final String key, final String value) {
        this.headers.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    /**
     * Adds multiple header values to the exception.
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
     * @return the exception's headers
     */
    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }
}
