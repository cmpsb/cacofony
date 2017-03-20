package net.cmpsb.cacofony.http;

import net.cmpsb.cacofony.http.exception.HttpException;
import net.cmpsb.cacofony.http.response.ResponseCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
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
     * A regex that matches HTTP versions.
     */
    private static final String VERSION_REGEX = "HTTP/(?<major>[0-9]+)\\.(?<minor>[0-9]+)";

    /**
     * A pattern that matches HTTP versions.
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile(VERSION_REGEX);

    /**
     * Parse an incoming request.
     *
     * @param in the input stream
     *
     * @return the request
     *
     * @throws IOException if an I/O error occurs when parsing
     */
    public Request parse(final BufferedReader in) throws IOException {
        // Read the start line and split it into the three components.
        final String startLine = in.readLine();
        final String[] startLineParts = SPACE_PATTERN.split(startLine, 3);

        // Error if the request line is too short.
        if (startLineParts.length < 3) {
            throw new HttpException(ResponseCode.BAD_REQUEST, "Invalid request line.");
        }

        // Parse the method. Error if the method is unknown.
        final Method method;
        try {
            method = Method.get(startLineParts[0]);
        } catch (final IllegalArgumentException ex) {
            throw new HttpException(ResponseCode.BAD_REQUEST, "Invalid method.");
        }

        // Get the path. Don't unescape it yet, that's for later.
        final String path = startLineParts[1];

        // Check the request version.
        final String version = startLineParts[2];
        final Matcher versionMatcher = VERSION_PATTERN.matcher(version);

        if (!versionMatcher.matches()) {
            throw new HttpException(ResponseCode.HTTP_VERSION_NOT_SUPPORTED,
                    "The version is in an unknown format.");
        }

        // Parse the numbers into integers.
        final int versionMajor = Integer.parseInt(versionMatcher.group("major"));
        final int versionMinor = Integer.parseInt(versionMatcher.group("minor"));

        if (versionMajor == 0 || versionMajor == 1) {
            return this.handleV1(in, method, path, versionMajor, versionMinor);
        }
        else {
            throw new HttpException(ResponseCode.HTTP_VERSION_NOT_SUPPORTED,
                    "This server only supports HTTP up to version 2.");
        }
    }

    /**
     * Handle a HTTP/0.9, HTTP/1.0 or a HTTP/1.1 request.
     *
     * @param in           the client's input stream
     * @param method       the request method
     * @param path         the request path
     * @param versionMajor the request version's major component
     * @param versionMinor the request version's minor component
     *
     * @return the parsed request
     *
     * @throws IOException if an I/O exception occurs while reading from the socket
     */
    private Request handleV1(final BufferedReader in, final Method method, final String path,
                             final int versionMajor, final int versionMinor) throws IOException {
        final Request request = new Request(method, path, versionMajor, versionMinor);

        // Read the headers first.
        while (true) {
            final String line = in.readLine();

            // Stop reading headers if the next line is empty.
            if (line.isEmpty()) {
                break;
            }

            // Find where the name ends and the value starts. Parse them.
            final int colonIdx = line.indexOf(':');

            // Error if there's no colon, indicating an obs-fold.
            if (colonIdx == -1) {
                throw new HttpException(ResponseCode.BAD_REQUEST, "Obs-folding is prohibited.");
            }

            final String key = line.substring(0, colonIdx);
            final String value = line.substring(colonIdx, line.length()).trim();

            // If the header key ends with a space, refuse to continue.
            if (key.endsWith(" ")) {
                throw new HttpException(ResponseCode.BAD_REQUEST,
                        "Illegal space after header key.");
            }

            // Add it to the collection.
            request.getHeaders().computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }

        // Read the body.
        final String encoding = request.getHeader("Transfer-Encoding");
        if (encoding != null) {

        }

        return request;
    }
}
