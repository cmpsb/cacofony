package net.wukl.cacofony.http.request;

import net.wukl.cacofony.http.encoding.TransferEncoding;
import net.wukl.cacofony.http.exception.BadRequestException;
import net.wukl.cacofony.http.exception.NotImplementedException;
import net.wukl.cacofony.http.exception.HttpException;
import net.wukl.cacofony.http.response.ResponseCode;
import net.wukl.cacofony.io.ChunkedInputStream;
import net.wukl.cacofony.io.HttpInputStream;
import net.wukl.cacofony.io.StreamHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser for HTTP requests.
 *
 * @author Luc Everse
 */
public class RequestParser {
    /**
     * A regex that matches a single ASCII space and nothing else.
     */
    private static final String SPACE_REGEX = " ";

    /**
     * A pattern that matches a single ASCII pattern and nothing else.
     */
    private static final Pattern SPACE_PATTERN = Pattern.compile(SPACE_REGEX);

    /**
     * A regex that can split comma-separated headers.
     */
    private static final String COMMA_SEPARATOR_REGEX = ",\\s*";

    /**
     * A pattern that can split comma-separated headers.
     */
    private static final Pattern COMMA_SEPARATOR_PATTERN = Pattern.compile(COMMA_SEPARATOR_REGEX);

    /**
     * A regex that matches HTTP versions.
     */
    private static final String VERSION_REGEX = "HTTP/(?<major>[0-9]+)\\.(?<minor>[0-9]+)";

    /**
     * A pattern that matches HTTP versions.
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile(VERSION_REGEX);

    /**
     * The header parser to use.
     */
    private final HeaderParser headerParser;

    /**
     * The stream helper to use.
     */
    private final StreamHelper streamHelper;

    /**
     * Creates a new HTTP request parser.
     *
     * @param headerParser the header parser to use
     * @param streamHelper the stream helper to use
     */
    public RequestParser(final HeaderParser headerParser, final StreamHelper streamHelper) {
        this.headerParser = headerParser;
        this.streamHelper = streamHelper;
    }

    /**
     * Parses an incoming request.
     *
     * @param in the input stream
     *
     * @return the request
     *
     * @throws IOException if an I/O error occurs when parsing
     */
    public MutableRequest parse(final HttpInputStream in) throws IOException {
        // Read the start line and split it into the three components.
        final String startLine = in.readLine();

        // An empty start line probably means that the stream reached EOF before it could
        // read a full line.
        if (startLine.isEmpty()) {
            throw new IOException("Client has closed the connection.");
        }

        final String[] startLineParts = SPACE_PATTERN.split(startLine, 3);

        // Error if the request line is too short.
        if (startLineParts.length < 3) {
            throw new HttpException(ResponseCode.BAD_REQUEST,
                    "Invalid request line \"" + startLine + "\".");
        }

        // Parse the method. Error if the method is unknown.
        final Method method;
        try {
            method = Method.get(startLineParts[0]);
        } catch (final IllegalArgumentException ex) {
            throw new HttpException(ResponseCode.BAD_REQUEST,
                    "Invalid method \"" + startLineParts[0] + "\"");
        }

        // Get the path. Don't unescape it yet, that's for later.
        final String path = startLineParts[1];

        // Check the request version.
        final String version = startLineParts[2];
        final Matcher versionMatcher = VERSION_PATTERN.matcher(version);

        if (!versionMatcher.matches()) {
            throw new HttpException(ResponseCode.HTTP_VERSION_NOT_SUPPORTED,
                    "The version is in an unknown format: \"" + version
                    + "\" does not match \"HTTP/x.x\".");
        }

        // Parse the numbers into integers.
        final int versionMajor = Integer.parseInt(versionMatcher.group("major"));
        final int versionMinor = Integer.parseInt(versionMatcher.group("minor"));

        final MutableRequest request = new MutableRequest(method, path, versionMajor, versionMinor);

        if (versionMajor == 0 || versionMajor == 1) {
            this.handleV1(in, request);
        } else {
            throw new HttpException(ResponseCode.HTTP_VERSION_NOT_SUPPORTED,
                    "This server only supports HTTP up to version 2.");
        }

        return request;
    }

    /**
     * Handles a HTTP/0.9, HTTP/1.0 or a HTTP/1.1 request.
     *
     * @param in      the client's input stream
     * @param request the request to write the data to
     *
     * @throws IOException if an I/O exception occurs while reading from the socket
     */
    private void handleV1(final HttpInputStream in, final MutableRequest request)
            throws IOException {

        // Read the headers first.
        final Map<String, List<String>> headers = this.headerParser.parse(in);
        request.adoptHeaders(headers);

        // Build the stack of input streams to read the message body.
        final List<String> teHeaders = request.getHeaders("Transfer-Encoding");
        if (teHeaders != null) {
            if (request.hasHeader("Content-Length")) {
                throw new BadRequestException("Both Transfer-Encoding and Content-Length present.");
            }

            request.setContentLength(-1);
            this.stackBodyStreams(in, teHeaders, request);
        } else {
            // This is a plain message. There must be a content-length value.
            final String contentLengthString = request.getHeader("Content-Length");
            final long contentLength;

            // If the header is missing, assume that there is no content.
            if (contentLengthString == null) {
                contentLength = 0;
            } else {
                contentLength = Long.parseLong(contentLengthString);
            }

            request.setContentLength(contentLength);
            request.setBody(in);
        }
    }

    /**
     * Builds the tree of body-processing streams for a request with a set of transfer encodings.
     *
     * @param in        the main input stream
     * @param teHeaders the request's set of Transfer-Encoding headers
     * @param request   the request object
     *
     * @throws IOException if an I/O error occurs while reading
     */
    private void stackBodyStreams(final HttpInputStream in,
                                  final List<String> teHeaders,
                                  final MutableRequest request) throws IOException {
        final List<String> encodings = new ArrayList<>();

        // Read all given values into an ordered list.
        for (final String line : teHeaders) {
            final String[] values = COMMA_SEPARATOR_PATTERN.split(line);
            Collections.addAll(encodings, values);
        }

        Collections.reverse(encodings);

        InputStream userStream = in;

        for (final String rawEncoding : encodings) {
            final TransferEncoding encoding = TransferEncoding.get(rawEncoding);

            if (encoding == TransferEncoding.CHUNKED) {
                userStream = new ChunkedInputStream(
                        this.streamHelper.makeLineAware(userStream),
                        request,
                        this.headerParser
                );
            } else if (encoding != null) {
                userStream = encoding.construct(userStream);
            } else {
                throw new NotImplementedException("Transfer encoding \"" + rawEncoding + "\"");
            }
        }

        request.setBody(userStream);
    }
}
