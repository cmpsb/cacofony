package net.cmpsb.cacofony.route;

import net.cmpsb.cacofony.http.cookie.CookieParser;
import net.cmpsb.cacofony.http.exception.NotFoundException;
import net.cmpsb.cacofony.http.request.Method;
import net.cmpsb.cacofony.http.request.MutableRequest;
import net.cmpsb.cacofony.http.request.QueryStringParser;
import net.cmpsb.cacofony.http.request.RequestPreparer;
import net.cmpsb.cacofony.http.response.EmptyResponse;
import net.cmpsb.cacofony.http.response.Response;
import net.cmpsb.cacofony.http.response.TextResponse;
import net.cmpsb.cacofony.mime.MimeParser;
import net.cmpsb.cacofony.mime.MimeType;
import net.cmpsb.cacofony.mime.StrictMimeParser;
import net.cmpsb.cacofony.util.Ob;
import net.cmpsb.cacofony.util.UrlCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
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
        this.router = new Router(parser, preparer);
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

        final RoutingEntry entry = new RoutingEntry(
            "test", path, action, Collections.singletonList(Method.GET), Collections.emptyList()
        );

        this.router.addRoute(entry);
    }

    private void populatePicky() {
        final PathCompiler compiler = new PathCompiler();
        final CompiledPath path = compiler.compile("/", Ob.map());

        final RouteAction getTextAction = request -> new TextResponse("get text");
        final RouteAction putTextAction = request -> new TextResponse("put text");
        final RouteAction getMpegAction = request -> new TextResponse("get mpeg");

        final RoutingEntry getTextEntry = new RoutingEntry(
                "test_get_text", path, getTextAction, Collections.singletonList(Method.GET),
                Collections.singletonList(MimeType.text())
        );

        final RoutingEntry putTextEntry = new RoutingEntry(
                "test_put_text", path, putTextAction, Collections.singletonList(Method.PUT),
                Collections.singletonList(MimeType.text())
        );

        final RoutingEntry getMpegEntry = new RoutingEntry(
                "test_get_mpeg", path, getMpegAction, Collections.singletonList(Method.GET),
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

        final RouteAction helloAction = request -> new TextResponse("hello");

        final RoutingEntry helloEntry = new RoutingEntry(
            "test_hello", helloPath, helloAction, Arrays.asList(Method.GET, Method.POST),
            Collections.singletonList(MimeType.text())
        );

        this.router.addRoute(helloEntry);
    }
}
