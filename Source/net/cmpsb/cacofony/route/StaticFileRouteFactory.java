package net.cmpsb.cacofony.route;

import net.cmpsb.cacofony.http.exception.NotFoundException;
import net.cmpsb.cacofony.http.request.HeaderValueParser;
import net.cmpsb.cacofony.http.request.Method;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.http.response.file.FileResponse;
import net.cmpsb.cacofony.http.response.ResponseCode;
import net.cmpsb.cacofony.http.response.file.RangeParser;
import net.cmpsb.cacofony.mime.MimeDb;
import net.cmpsb.cacofony.mime.MimeParser;
import net.cmpsb.cacofony.mime.MimeType;
import net.cmpsb.cacofony.util.Ob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 *
 * @author Luc Everse
 */
public class StaticFileRouteFactory {
    private static final Logger logger = LoggerFactory.getLogger(StaticFileRouteFactory.class);

    /**
     * The path compiler to use.
     */
    private final PathCompiler compiler;

    /**
     * The MIME parser to use.
     */
    private final MimeParser parser;

    /**
     * The MIME DB to use.
     */
    private final MimeDb mimeDb;

    /**
     * The header value parser to use.
     */
    private final HeaderValueParser valueParser;

    /**
     * The range parser to use.
     */
    private final RangeParser rangeParser;

    /**
     * Creates a new factory for routes serving static files.
     *
     * @param compiler    the path compiler to use
     * @param parser      the MIME parser to use
     * @param mimeDb      the MIME DB to use
     * @param valueParser the header value parser to use
     * @param rangeParser the range parser to use
     */
    public StaticFileRouteFactory(final PathCompiler compiler,
                                  final MimeParser parser,
                                  final MimeDb mimeDb,
                                  final HeaderValueParser valueParser,
                                  final RangeParser rangeParser) {
        this.compiler = compiler;
        this.parser = parser;
        this.mimeDb = mimeDb;
        this.valueParser = valueParser;
        this.rangeParser = rangeParser;
    }

    /**
     * Builds a routing entry serving static files.
     *
     * @param prefix the URL prefix the static files should be accessible for
     * @param dir    the local directory the files should be in
     *
     * @return a routing entry serving files
     */
    public RoutingEntry build(final String prefix, final String dir) {
        final String name = "static_file_route_" + prefix;

        final String parameterizedPath = prefix + "/{file}";
        final CompiledPath path = this.compiler.compile(parameterizedPath, Ob.map("file", ".+"));

        final RouteAction action = this.createAction(dir);

        final List<Method> methods = Arrays.asList(Method.GET, Method.HEAD);
        final List<MimeType> mimeTypes = Collections.singletonList(MimeType.any());

        return new RoutingEntry(name, path, action, methods, mimeTypes);
    }

    /**
     * Generates a routing action that should serve a file.
     *
     * @param localDir     the local directory the files should be in
     *
     * @return an action capable of serving a file
     */
    private RouteAction createAction(final String localDir) {
        return request -> {
            try {
                final String path = localDir + '/' + request.getPathParameter("file");
                final File file = new File(path);

                final FileResponse response = new FileResponse(new File(path));

                response.setRanges(this.rangeParser.parse(request, response.getContentLength()));
                response.setContentType(this.guessType(file, path));

                if (this.can304(request, response)) {
                    response.setStatus(ResponseCode.NOT_MODIFIED);
                }

                return response;
            } catch (final FileNotFoundException ex) {
                throw new NotFoundException(ex);
            }
        };
    }

    /**
     * Guesses the MIME type for a file.
     *
     * @param file the file to guess the type for
     * @param name the name of the file
     *
     * @return the file's MIME type
     */
    private MimeType guessType(final File file, final String name) {
        String fileType = null;

        try (InputStream in = new FileInputStream(file)) {
            fileType = URLConnection.guessContentTypeFromStream(in);
        } catch (final IOException ex) {
            logger.warn("I/O exception while guessing the content type of \"{}\".", name, ex);
            // Ignore and move on to the next method.
        }

        if (fileType == null) {
            fileType = URLConnection.guessContentTypeFromName(name);
        }

        if (fileType == null) {
            return this.mimeDb.getForName(name);
        }

        return this.parser.parse(fileType);
    }

    /**
     * Returns whether the controller may return a HTTP 304.
     *
     * @param request  the request
     * @param response the response that may be a 304
     *
     * @return true if the response can be an HTTP 304, otherwise false
     */
    private boolean can304(final Request request, final FileResponse response) {
        final List<String> etags = this.valueParser.parseCommaSeparated(request, "If-None-Match");

        return etags.contains(response.getEtag());
    }
}
