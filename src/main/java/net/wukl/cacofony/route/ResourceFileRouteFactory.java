package net.wukl.cacofony.route;

import net.wukl.cacofony.controller.Controller;
import net.wukl.cacofony.http.request.HeaderValueParser;
import net.wukl.cacofony.http.request.Method;
import net.wukl.cacofony.http.request.Request;
import net.wukl.cacofony.http.response.Response;
import net.wukl.cacofony.http.response.ResponseCode;
import net.wukl.cacofony.http.response.file.ResourceResponse;
import net.wukl.cacofony.mime.MimeGuesser;
import net.wukl.cacofony.mime.MimeType;
import net.wukl.cacofony.util.Ob;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Luc Everse
 */
public class ResourceFileRouteFactory extends FileRouteFactory {
    /**
     * The path compiler to use.
     */
    private final PathCompiler compiler;

    /**
     * The MIME guesser to use.
     */
    private final MimeGuesser mimeGuesser;

    /**
     * Creates a new factory for routes serving resource files.
     *
     * @param compiler    the path compiler to use
     * @param valueParser the header value parser to use
     * @param mimeGuesser the MIME guesser to use
     */
    public ResourceFileRouteFactory(final PathCompiler compiler,
                                    final HeaderValueParser valueParser,
                                    final MimeGuesser mimeGuesser) {
        super(valueParser);

        this.compiler = compiler;
        this.mimeGuesser = mimeGuesser;
    }

    /**
     * Builds a routing entry serving static files.
     *
     * @param prefix the URL prefix the static files should be accessible for
     * @param jar    the jar containing the resources
     * @param base   the base directory the resources should be in
     *
     * @return a routing entry serving files
     */
    public RoutingEntry build(final String prefix, final Class<?> jar, final String base) {
        final String name = "resource_file_route_" + prefix;

        final String parameterizedPath = prefix + "/{file}";
        final CompiledPath path = this.compiler.compile(parameterizedPath, Ob.map("file", ".+"));

        final Controller controller = new ResourceFileController(jar, base);
        final java.lang.reflect.Method method;
        try {
             method = ResourceFileController.class.getMethod("handle", Request.class);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        final List<Method> methods = Arrays.asList(Method.GET, Method.HEAD);
        final List<MimeType> mimeTypes = Collections.singletonList(MimeType.any());

        return new RoutingEntry(name, path, controller, method, methods, mimeTypes);
    }

    /**
     * The virtual controller to serve these routes.
     */
    private class ResourceFileController extends Controller {
        /**
         * The base directory for the resources.
         */
        private final String base;

        /**
         * The jar containing the resources.
         */
        private final Class<?> jar;

        /**
         * The MIME guesser to use.
         */
        private final MimeGuesser mimeGuesser;

        /**
         * The file's modification date.
         */
        private final long modificationDate;

        /**
         * Creates a new file resource controller.
         *
         * @param jar the jar containing the resources
         * @param base the base directory for the resources
         */
        ResourceFileController(final Class<?> jar, final String base) {
            this.base = base;
            this.jar = jar;

            this.modificationDate = Instant.now().toEpochMilli();
            this.mimeGuesser = ResourceFileRouteFactory.this.mimeGuesser;
        }

        /**
         * Handles the request.
         *
         * @param request the request
         *
         * @return the response
         */
        public Response handle(final Request request) {
            final String file = this.base + '/' + request.getPathParameter("file");

            final ResourceResponse response =
                    new ResourceResponse(this.jar, file, this.modificationDate);

            // Guess and set the content type too.
            response.setContentType(this.mimeGuesser.guessLocal(this.jar, file));

            // If the client indicates it may have cached the file and it actually did,
            // reply Not Modified.
            if (ResourceFileRouteFactory.this.can304(request, response)) {
                response.setStatus(ResponseCode.NOT_MODIFIED);
            }

            return response;
        }
    }
}
