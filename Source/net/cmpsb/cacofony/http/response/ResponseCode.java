package net.cmpsb.cacofony.http.response;

/**
 * A HTTP response code.
 *
 * @author Luc Everse
 */
public enum ResponseCode {
    /**
     * The initial part of the request has been received and the server awaits the rest.
     */
    CONTINUE(100, "Continue"),

    /**
     * The server understands the Upgrade request and tries to execute it.
     */
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),

    /**
     * The server is processing the request, but has not yet completed it.
     * WebDAV.
     */
    PROCESSING(102, "Processing"),

    /**
     * The request has succeeded.
     */
    OK(200, "OK"),

    /**
     * The request has succeeded and one or more resources have been created.
     */
    CREATED(201, "Created"),

    /**
     * The request was accepted for processing, but the server has not finished yet.
     */
    ACCEPTED(202, "Accepted"),

    /**
     * The request has succeeded, but was modified by a transforming proxy.
     */
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),

    /**
     * The request has succeeded and the server has no additional content to transfer.
     */
    NO_CONTENT(204, "No Content"),

    /**
     * The request has succeeded and the server requests that the client resets the document
     * that generated the request.
     */
    RESET_CONTENT(205, "Reset Content"),

    /**
     * The request has succeeded and the requested range(s) will be returned.
     */
    PARTIAL_CONTENT(206, "Partial Content"),

    /**
     * The request returned multiple statuses for multiple operations.
     * WebDAV.
     */
    MULTI_STATUS(207, "Multi Status"),

    /**
     * The collection was already returned.
     * WebDAV.
     */
    ALREADY_REPORTED(208, "Already Reported"),

    /**
     * A differential instance manipulation was used to produce the response.
     */
    IM_USED(226, "IM Used"),

    /**
     * The target resource has more than one representation.
     */
    MULTIPLE_CHOICES(300, "Multiple Choices"),

    /**
     * The target has a new permanent URI.
     */
    MOVED_PERMANENTLY(301, "Moved Permanently"),

    /**
     * The target resides temporarily under a different URI.
     */
    FOUND(302, "Found"),

    /**
     * The server is redirecting the client to a different resource which is intended to provide
     * an indirect response to the original request.
     */
    SEE_OTHER(303, "See Other"),

    /**
     * The condition would have succeeded if the precondition had not evaluated to false.
     */
    NOT_MODIFIED(304, "Not Modified"),

    /**
     * The resource must be accessed through the proxy indicated by the Location header.
     *
     * @deprecated due to security concerns
     */
    @Deprecated
    USE_PROXY(305, "Use Proxy"),

    /**
     * The resource resides temporarily under a different URI and the client may not change methods.
     */
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),

    /**
     * The target has a new permanent URI and the client may not change methods.
     */
    PERMANENT_REDIRECT(308, "Permanent Redirect"),

    /**
     * The server can not process the request because of a client error.
     */
    BAD_REQUEST(400, "Bad Request"),

    /**
     * The request has not been applied because it lacks valid authentication credentials.
     */
    UNAUTHORIZED(401, "Unauthorized"),

    /**
     * Payment is required in order to access the resource.
     */
    PAYMENT_REQUIRED(402, "Payment Required"),

    /**
     * The server understood the request but refuses to authorize it.
     */
    FORBIDDEN(403, "Forbidden"),

    /**
     * The server is unable to find the resource or is unwilling to disclose that it exists.
     */
    NOT_FOUND(404, "Not Found"),

    /**
     * The method in the request start line is understood, but not supported for the resource.
     */
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),

    /**
     * The target resource does not have a representation that would be acceptable to the client.
     */
    NOT_ACCEPTABLE(406, "Not Acceptable"),

    /**
     * The request has not been applied because it lacks valid proxy authentication credentials.
     */
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),

    /**
     * The server did not complete a request within the time it was prepared to wait.
     */
    REQUEST_TIMEOUT(408, "Request Timeout"),

    /**
     * The request could not be completed due to a conflict with the current state of the resource.
     */
    CONFLICT(409, "Conflict"),

    /**
     * Access to the target is no longer available and this condition is likely to be permanent.
     */
    GONE(410, "Gone"),

    /**
     * The server refuses to accept the request without a Content-Length header.
     */
    LENGTH_REQUIRED(411, "Length Required"),

    /**
     * One ore more conditions in the request header evaluated to false on the server.
     */
    PRECONDITION_FAILED(412, "Precondition Failed"),

    /**
     * The server has refused the request since the payload is larger than the server can accept.
     */
    PAYLOAD_TOO_LARGE(413, "Payload Too Large"),

    /**
     * The server has refused the request because the URI is longer than the server is willing to
     * interpret.
     */
    URI_TOO_LONG(414, "URI Too Long"),

    /**
     * The payload format is not supported by this method on this target.
     */
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),

    /**
     * The server refused to serve the requested range(s).
     */
    RANGE_NOT_SATISFIABLE(416, "Range Not Satisfiable"),

    /**
     * The expectation in the Expect header could not be met.
     */
    EXPECTATION_FAILED(417, "Expectation Failed"),

    /**
     * The request was directed to a server that was not able to produce a response.
     */
    MISDIRECTED_REQUEST(421, "Misdirected Request"),

    /**
     * The server was unable to process the enclosed entity.
     * WebDAV.
     */
    UNPROCESSABLE_ENTITY(422, "Unprocessable Entity"),

    /**
     * The source or destination resource is locked.
     * WebDAV.
     */
    LOCKED(423, "Locked"),

    /**
     * One or more dependencies failed.
     * WebDAV.
     */
    FAILED_DEPENDENCY(424, "Failed Dependency"),

    /**
     * The server refuses to perform the Request using the current protocol.
     */
    UPGRADE_REQUIRED(426, "Upgrade Required"),

    /**
     * The server requires the Request to be conditional.
     */
    PRECONDITION_REQUIRED(427, "Precondition Required"),

    /**
     * The user has sent too many requests in a given amount of time.
     */
    TOO_MANY_REQUESTS(429, "Too Many Requests"),

    /**
     * The server refuses to process the requests because the header fields are too large.
     */
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),

    /**
     * The server is denying access to the resource as a consequence of a legal demand.
     */
    UNAVAILABLE_FOR_LEGAL_REASONS(451, "Unavailable For Legal Reasons"),

    /**
     * The server encountered an unexpected condition and cannot complete the request.
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),

    /**
     * The server does not support the required functionality to fulfill the request.
     */
    NOT_IMPLEMENTED(501, "Not Implemented"),

    /**
     * The server, while acting as a gateway or proxy, received an invalid response.
     */
    BAD_GATEWAY(502, "Bad Gateway"),

    /**
     * The server is currently unable to handle the response due to difficulties or maintenance.
     */
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),

    /**
     * The server, while acting as a gateway or proxy, did not receive a timely response.
     */
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),

    /**
     * The server does not support the major version of HTTP that was requested.
     */
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),

    /**
     * The chosen resource engages in transparent content negotiation itself.
     */
    VARIANT_ALSO_NEGOTIATES(506, "Variant Also Negotiates"),

    /**
     * The method could not be performed on the resource because the server is unable to store it.
     * WebDAV.
     */
    INSUFFICIENT_STORAGE(507, "Insufficient Storage"),

    /**
     * The server encountered an infinite loop while processing the request.
     * WebDAV.
     */
    LOOP_DETECTED(508, "Loop Detected"),

    /**
     * The policy for accessing the resource has not been met in the request.
     */
    NOT_EXTENDED(510, "Not Extended"),

    /**
     * the client needs to authenticate to gain network access.
     */
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required");

    /**
     * The numeric code.
     */
    private int code;

    /**
     * The programmer-friendly description of the status code.
     */
    private String description;

    /**
     * Create a new response code.
     *
     * @param code        the numeric status code
     * @param description the code's description
     */
    ResponseCode(final int code, final String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * @return the numeric status code
     */
    public int getCode() {
        return this.code;
    }

    /**
     * @return the programmer-friendly description
     */
    public String getDescription() {
        return this.description;
    }
}
