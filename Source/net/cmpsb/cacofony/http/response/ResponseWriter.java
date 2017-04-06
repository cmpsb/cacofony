package net.cmpsb.cacofony.http.response;

import net.cmpsb.cacofony.http.encoding.TransferEncoding;
import net.cmpsb.cacofony.http.request.HeaderValueParser;
import net.cmpsb.cacofony.http.request.Method;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.io.ChunkedOutputStream;
import net.cmpsb.cacofony.server.ServerSettings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author Luc Everse
 */
public class ResponseWriter {
    /**
     * The server's settings.
     */
    private final ServerSettings settings;

    /**
     * The header value parser to use.
     */
    private final HeaderValueParser headerValueParser;

    /**
     * Creates a new response writer.
     *
     * @param settings          the server settings
     * @param headerValueParser the header value parser to use
     */
    public ResponseWriter(final ServerSettings settings,
                          final HeaderValueParser headerValueParser) {
        this.settings = settings;
        this.headerValueParser = headerValueParser;
    }

    /**
     * Writes the response's response line and headers to the client.
     *
     * @param request  the request that triggered this response
     * @param response the response to write
     * @param out      the client
     *
     * @throws IOException if an I/O error occurs
     */
    private void writeHeaders(final Request request,
                              final Response response,
                              final OutputStream out) throws IOException {
        final StringBuilder headerBuilder = new StringBuilder();

        // Get the client's HTTP version. If it's unavailable, use HTTP/1.0.
        final int majorVersion;
        final int minorVersion;
        if (request != null) {
            majorVersion = request.getMajorVersion();
            minorVersion = request.getMinorVersion();
        } else {
            majorVersion = 1;
            minorVersion = 0;
        }

        // Write the status line.
        headerBuilder.append("HTTP/");
        headerBuilder.append(majorVersion);
        headerBuilder.append('.');
        headerBuilder.append(minorVersion);
        headerBuilder.append(' ');
        headerBuilder.append(response.getStatus().getCode());
        headerBuilder.append(' ');
        headerBuilder.append(response.getStatus().getDescription());
        headerBuilder.append("\r\n");

        final Map<String, List<String>> headers = response.getHeaders();

        for (final String key : headers.keySet()) {
            for (final String value : headers.get(key)) {
                headerBuilder.append(key);
                headerBuilder.append(": ");
                headerBuilder.append(value);
                headerBuilder.append("\r\n");
            }
        }

        headerBuilder.append("\r\n");

        out.write(headerBuilder.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Writes a response out to the client.
     *
     * @param request  the request that triggered this response
     * @param response the response to write
     * @param out      the client
     *
     * @return the top-level output stream used to write the response
     *
     * @throws IOException if an I/O error occurs
     */
    public OutputStream write(final Request request,
                              final Response response,
                              final OutputStream out) throws IOException {
        final long contentLength = response.getContentLength();
        if (contentLength >= 0) {
            return this.writePlainResponse(request, response, out);
        } else {
            return this.writeEncodedResponse(request, response, out);
        }
    }

    /**
     * Writes the response out to the client using a known size.
     *
     * @param request  the request that triggered this response
     * @param response the response to write
     * @param out      the client
     *
     * @return the stream used to write the response
     *
     * @throws IOException if an I/O error occurs
     */
    private OutputStream writePlainResponse(final Request request,
                                            final Response response,
                                            final OutputStream out) throws IOException {
        final Method method;
        if (request == null) {
            method = Method.GET;
        } else {
            method = request.getMethod();
        }

        final TransferEncoding aeEncoding = this.getAcceptableEncodings(request, "Accept-Encoding");
        if (this.canCompress(response) && aeEncoding != null) {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            final OutputStream compressor = aeEncoding.construct(buffer);
            response.write(compressor);
            compressor.close();

            response.setHeader("Content-Encoding", aeEncoding.getHttpName());
            response.setHeader("Content-Length", String.valueOf(buffer.size()));

            this.writeHeaders(request, response, out);

            if (method != Method.HEAD) {
                out.write(buffer.toByteArray());
            }

            return out;
        } else {
            response.setHeader("Content-Length", String.valueOf(response.getContentLength()));

            this.writeHeaders(request, response, out);

            if (method != Method.HEAD) {
                response.write(out);
            }

            return out;
        }
    }

    /**
     * Writes the respones out to the client following a requested list of transfer encodings.
     *
     * @param request  the request that triggered this response
     * @param response the response to write
     * @param out      the client
     *
     * @return the stream used to write the response
     *
     * @throws IOException if an I/O error occurs
     */
    private OutputStream writeEncodedResponse(final Request request,
                                              final Response response,
                                              final OutputStream out) throws IOException {
        final OutputStream target =
                this.applyChunkedCompression(request, response, new ChunkedOutputStream(out));

        this.writeHeaders(request, response, out);

        if (request.getMethod() != Method.HEAD) {
            response.write(target);

            return target;
        }

        return out;
    }

    /**
     * Checks whether the writer can compress a response.
     *
     * @param response the response to possibly compress
     *
     * @return true if the response can be compressed, false otherwise
     */
    private boolean canCompress(final Response response) {
        return this.settings.isCompressionEnabled()
            && response.isCompressionAllowed(this.settings.canCompressByDefault());
    }

    /**
     * Applies, if possible, a content compression to the stream.
     *
     * @param request  the request
     * @param response the response
     * @param target   the chunked output stream the compressor should write to
     *
     * @return a compressing output stream or, if compression is not available, {@code target}
     *
     * @throws IOException if an I/O error occurs
     */
    private OutputStream applyChunkedCompression(final Request request,
                                                 final Response response,
                                                 final OutputStream target) throws IOException {
        if (!this.canCompress(response)) {
            response.setHeader("Transfer-Encoding", "chunked");
            return target;
        }

        final TransferEncoding teEncoding = this.getAcceptableEncodings(request, "TE");
        if (teEncoding != null) {
            response.setHeader("Transfer-Encoding", teEncoding.getHttpName() + ", chunked");
            return teEncoding.construct(target);
        }

        response.setHeader("Transfer-Encoding", "chunked");

        final TransferEncoding aeEncoding = this.getAcceptableEncodings(request, "Accept-Encoding");
        if (aeEncoding != null) {
            response.setHeader("Content-Encoding", aeEncoding.getHttpName());
            return aeEncoding.construct(target);
        }

        return target;
    }

    /**
     * Determines the first acceptable and supported compression encoding for a header field.
     * <p>
     * {@code header} must be either {@code TE} or {@code Accept-Encoding}. Other values may work,
     * but why would you do that?
     *
     * @param request the original request
     * @param header  the header to check
     *
     * @return the first acceptable compression encoding or {@code null}
     */
    private TransferEncoding getAcceptableEncodings(final Request request, final String header) {
        if (request == null) {
            return null;
        }

        return this.headerValueParser.parseCommaSeparated(request, header)
                .stream()
                .map(TransferEncoding::get)
                .distinct()
                .filter(this.settings.getCompressionAlgorithms()::contains)
                .findFirst()
                .orElse(null);
    }


}
