package net.cmpsb.cacofony.route;

import net.cmpsb.cacofony.controller.Controller;
import net.cmpsb.cacofony.http.exception.NotFoundException;
import net.cmpsb.cacofony.http.request.HeaderValueParser;
import net.cmpsb.cacofony.http.request.Method;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.http.response.Response;
import net.cmpsb.cacofony.http.response.file.FileResponse;
import net.cmpsb.cacofony.http.response.ResponseCode;
import net.cmpsb.cacofony.http.response.file.RangeParser;
import net.cmpsb.cacofony.mime.MimeGuesser;
import net.cmpsb.cacofony.mime.MimeType;
import net.cmpsb.cacofony.util.Ob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A factory for static file-serving routes.
 *
 * @author Luc Everse
 */
public class StaticFileRouteFactory extends FileRouteFactory {
    private static final Logger logger = LoggerFactory.getLogger(StaticFileRouteFactory.class);

    /**
     * The path compiler to use.
     */
    private final PathCompiler compiler;

    /**
     * The header value parser to use.
     */
    private final HeaderValueParser valueParser;

    /**
     * The range parser to use.
     */
    private final RangeParser rangeParser;

    /**
     * The MIME guesser to use.
     */
    private final MimeGuesser mimeGuesser;

    /**
     * Creates a new factory for routes serving static files.
     *
     * @param compiler    the path compiler to use
     * @param valueParser the header value parser to use
     * @param rangeParser the range parser to use
     * @param mimeGuesser the MIME guesser to use
     */
    public StaticFileRouteFactory(final PathCompiler compiler,
                                  final HeaderValueParser valueParser,
                                  final RangeParser rangeParser,
                                  final MimeGuesser mimeGuesser) {
        super(valueParser);

        this.compiler = compiler;
        this.valueParser = valueParser;
        this.rangeParser = rangeParser;
        this.mimeGuesser = mimeGuesser;
    }

    /**
     * Builds a routing entry serving static files.
     *
     * @param prefix the URL prefix the static files should be accessible for
     * @param dir    the local directory the files should be in
     *
     * @return a routing entry serving files
     */
    public RoutingEntry build(final String prefix, final Path dir) {
        final Path absDir;
        try {
            absDir = dir.toRealPath();
        } catch (final IOException ex) {
            throw new RuntimeException("Unable to get the canonical path for " + dir + ": ", ex);
        }

        final String name = "static_file_route_" + prefix;

        final String parameterizedPath = prefix + "/{file}";
        final CompiledPath path = this.compiler.compile(parameterizedPath, Ob.map("file", ".+"));

        final Controller controller = new FileController(absDir);
        final java.lang.reflect.Method method;
        try {
            method = FileController.class.getMethod("handle", Request.class);
        } catch (final NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }

        final List<Method> methods = Arrays.asList(Method.GET, Method.HEAD);
        final List<MimeType> mimeTypes = Collections.singletonList(MimeType.any());

        return new RoutingEntry(name, path, controller, method, methods, mimeTypes);
    }

    /**
     * The virtual controller serving these files.
     */
    private class FileController extends Controller {
        /**
         * The local directory the files should be in.
         */
        private final Path localDir;

        /**
         * The header value parser.
         */
        private final HeaderValueParser valueParser;

        /**
         * The Range header parser.
         */
        private final RangeParser rangeParser;

        /**
         * The MIME guesser.
         */
        private final MimeGuesser mimeGuesser;

        /**
         * Creates a new virtual file controller.
         *
         * @param localDir the local directory the files should be in
         */
        FileController(final Path localDir) {
            this.localDir = localDir;

            this.valueParser = StaticFileRouteFactory.this.valueParser;
            this.rangeParser = StaticFileRouteFactory.this.rangeParser;
            this.mimeGuesser = StaticFileRouteFactory.this.mimeGuesser;
        }

        /**
         * Handles the request.
         *
         * @param request the request
         *
         * @return the response
         */
        public Response handle(final Request request) {
            try {
                final Path file = this.localDir.resolve(request.getPathParameter("file"));

                // If the request attempts to traverse the directory tree or if the file just
                // doesn't exist reply Not Found.
                if (!file.startsWith(this.localDir) || !Files.exists(file)) {
                    throw new NotFoundException();
                }

                final FileResponse response = new FileResponse(file.toFile());

                // Set the request/response ranges, if any.
                final List<String> ranges = this.valueParser.parseCommaSeparated(request, "Range");
                response.setRanges(this.rangeParser.parse(ranges, response.getContentLength()));

                // Guess and set the content type too.
                response.setContentType(this.mimeGuesser.guessLocal(file));

                // If the client indicates it may have cached the file and it actually did,
                // reply Not Modified.
                if (StaticFileRouteFactory.this.can304(request, response)) {
                    response.setStatus(ResponseCode.NOT_MODIFIED);
                }

                return response;
            } catch (final FileNotFoundException ex) {
                // If anything happens to the file during this process reply a 404 too.
                throw new NotFoundException(ex);
            }
        }
    }
}
