package net.wukl.cacofony.route;

import net.wukl.cacofony.controller.Controller;
import net.wukl.cacofony.http.cookie.CookieParser;
import net.wukl.cacofony.http.exception.NotFoundException;
import net.wukl.cacofony.http.request.Method;
import net.wukl.cacofony.http.request.MutableRequest;
import net.wukl.cacofony.http.request.QueryStringParser;
import net.wukl.cacofony.http.request.Request;
import net.wukl.cacofony.http.request.RequestPreparer;
import net.wukl.cacofony.http.response.EmptyResponse;
import net.wukl.cacofony.http.response.Response;
import net.wukl.cacofony.http.response.TextResponse;
import net.wukl.cacofony.mime.MimeParser;
import net.wukl.cacofony.mime.MimeType;
import net.wukl.cacofony.mime.StrictMimeParser;
import net.wukl.cacofony.util.Ob;
import net.wukl.cacofony.util.UrlCodec;
import net.wukl.cacodi.DependencyResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the request router.
 *
 * @author Luc Everse
 */
public class RouterTest {
    private Router router;

    @BeforeEach
    public void before() {
        final MimeParser parser = new StrictMimeParser();
        final UrlCodec urlCodec = new UrlCodec();
        final CookieParser cookieParser = new CookieParser(urlCodec);
        final QueryStringParser queryStringParser = new QueryStringParser();
        final RequestPreparer preparer = new RequestPreparer(cookieParser, queryStringParser);
        final DependencyResolver resolver = new DependencyResolver();
        final ActionInvoker invoker = new ActionInvoker(resolver);
        this.router = new Router(parser, preparer, invoker);
    }

    @Test
    public void testNoRoutes() throws Exception {
        final MutableRequest request = new MutableRequest(Method.GET, "", 1, 1);
        assertThrows(NotFoundException.class, () -> this.router.handle(request));
    }

    @Test
    public void testSingleRoute() throws Exception {
        this.populateSimple();

        final MutableRequest request = new MutableRequest(Method.GET, "/", 1, 1);
        request.getHeaders().put("Accept", Collections.emptyList());

        final Response response = this.router.handle(request);

        assertThat(response).isNotNull().isInstanceOf(EmptyResponse.class);
    }

    @Test
    public void testAutoHead() throws Exception {
        this.populateSimple();

        final MutableRequest request = new MutableRequest(Method.HEAD, "/", 1, 1);

        final Response response = this.router.handle(request);

        assertThat(response).isNotNull().isInstanceOf(EmptyResponse.class);
    }

    @Test
    public void testPickyRoutes() throws Exception {
        this.populatePicky();

        final MutableRequest request = new MutableRequest(Method.GET, "/?id=3", 1, 1);
        request.getHeaders().put("accept", Collections.singletonList("audio/mpeg"));

        final Response response = this.router.handle(request);

        assertThat(response).isNotNull().isInstanceOf(TextResponse.class);

        final TextResponse textResponse = (TextResponse) response;

        assertThat(textResponse.getContent()).as("content").isEqualTo("get mpeg");
    }

    @Test
    public void testNoMatchingRoute() throws Exception {
        this.populateComplex();

        final MutableRequest request = new MutableRequest(Method.HEAD, "!!no_such_route!!", 1, 1);

        assertThrows(NotFoundException.class, () -> this.router.handle(request));
    }

    private void populateSimple() {
        final CompiledPath path =
                new CompiledPath("/", Pattern.compile("/"), Collections.emptyList());

        final RouteAction action = request -> new EmptyResponse();

        final RoutingEntry entry = this.createEntry(
                "test", path, "emptyAction", Collections.singletonList(Method.GET),
                Collections.emptyList()
        );

        this.router.addRoute(entry);
    }

    private void populatePicky() {
        final PathCompiler compiler = new PathCompiler();
        final CompiledPath path = compiler.compile("/", Ob.map());

        final RoutingEntry getTextEntry = this.createEntry(
                "test_get_text", path, "getTextAction", Collections.singletonList(Method.GET),
                Collections.singletonList(MimeType.text())
        );

        final RoutingEntry putTextEntry = this.createEntry(
                "test_put_text", path, "putTextAction", Collections.singletonList(Method.PUT),
                Collections.singletonList(MimeType.text())
        );

        final RoutingEntry getMpegEntry = this.createEntry(
                "test_get_mpeg", path, "getMpegAction", Collections.singletonList(Method.GET),
                Collections.singletonList(new MimeType("audio", "mpeg"))
        );

        this.router.addRoute(getTextEntry);
        this.router.addRoute(putTextEntry);
        this.router.addRoute(getMpegEntry);
    }

    private void populateComplex() {
        this.populatePicky();

        final PathCompiler compiler = new PathCompiler();
        final CompiledPath helloPath = compiler.compile("/hello/{name}", Ob.map("name", ".+"));

        final RoutingEntry helloEntry = this.createEntry(
                "test_hello", helloPath, "helloAction", Arrays.asList(Method.GET, Method.POST),
                Collections.singletonList(MimeType.text())
        );

        this.router.addRoute(helloEntry);
    }

    private RoutingEntry createEntry(final String name,
                                     final CompiledPath path,
                                     final String route,
                                     final List<Method> methods,
                                     final List<MimeType> contentTypes) {
        final Controller controller = new TestController();
        final java.lang.reflect.Method method;
        try {
            method = TestController.class.getMethod(route, Request.class);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return new RoutingEntry(name, path, controller, method, methods, contentTypes);
    }

    private class TestController extends Controller {
        public Response getTextAction(final Request request) {
            return new TextResponse("get text");
        }

        public Response putTextAction(final Request request) {
            return new TextResponse("put text");
        }

        public Response getMpegAction(final Request request) {
            return new TextResponse("get mpeg");
        }

        public Response emptyAction(final Request request) {
            return new EmptyResponse();
        }

        public Response helloAction(final Request request) {
            return new TextResponse("hello");
        }
    }
}
