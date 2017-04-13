package net.cmpsb.cacofony.route;

import net.cmpsb.cacofony.http.request.HeaderValueParser;
import net.cmpsb.cacofony.http.request.Method;
import net.cmpsb.cacofony.http.response.ResponseCode;
import net.cmpsb.cacofony.http.response.file.ResourceResponse;
import net.cmpsb.cacofony.mime.MimeGuesser;
import net.cmpsb.cacofony.mime.MimeType;
import net.cmpsb.cacofony.util.Ob;

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

        final RouteAction action = this.createAction(jar, base);

        final List<Method> methods = Arrays.asList(Method.GET, Method.HEAD);
        final List<MimeType> mimeTypes = Collections.singletonList(MimeType.any());

        return new RoutingEntry(name, path, action, methods, mimeTypes);
    }

    /**
     * Generates a routing action that should serve a file.
     *
     * @param jar  the class representing the jar containing the resources
     * @param base the base directory for the resources
     *
     * @return an action capable of serving a file
     */
    private RouteAction createAction(final Class<?> jar, final String base) {
        final long modificationDate = Instant.now().toEpochMilli();

        return request -> {
            final String file = base + '/' + request.getPathParameter("file");

            final ResourceResponse response = new ResourceResponse(jar, file, modificationDate);

            // Guess and set the content type too.
            response.setContentType(this.mimeGuesser.guessLocal(jar, file));

            // If the client indicates it may have cached the file and it actually did,
            // reply Not Modified.
            if (this.can304(request, response)) {
                response.setStatus(ResponseCode.NOT_MODIFIED);
            }

            return response;
        };
    }
}
