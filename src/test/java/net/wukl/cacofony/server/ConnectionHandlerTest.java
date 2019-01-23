package net.wukl.cacofony.server;

import net.wukl.cacofony.exception.ExceptionHandler;
import net.wukl.cacofony.http.exception.BadRequestException;
import net.wukl.cacofony.http.exception.HttpException;
import net.wukl.cacofony.http.request.Method;
import net.wukl.cacofony.http.request.MutableRequest;
import net.wukl.cacofony.http.request.RequestParser;
import net.wukl.cacofony.http.response.EmptyResponse;
import net.wukl.cacofony.http.response.Response;
import net.wukl.cacofony.http.response.ResponsePreparer;
import net.wukl.cacofony.http.response.ResponseWriter;
import net.wukl.cacofony.route.Router;
import net.wukl.cacofony.server.host.Host;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Stack;


import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Luc Everse
 */
public class ConnectionHandlerTest {
    private ConnectionHandler handler;
    private RequestParser parser;
    private Host defaultHost;
    private Router defaultRouter;
    private ExceptionHandler defaultExceptionHandler;
    private ResponsePreparer defaultPreparer;
    private ResponseWriter defaultWriter;
    private Host namedHost;
    private Router namedRouter;
    private ExceptionHandler namedExceptionHandler;
    private ResponsePreparer namedPreparer;
    private ResponseWriter namedWriter;
    private InetAddress address;
    private int port;
    private ByteArrayOutputStream out;
    private ByteArrayInputStream in;

    @BeforeEach
    public void before() throws Exception {
        this.parser = mock(RequestParser.class);
        this.handler = new ConnectionHandler(this.parser);

        this.defaultRouter = mock(Router.class);
        doReturn(new EmptyResponse()).when(this.defaultRouter).handle(any());

        this.defaultWriter = mock(ResponseWriter.class);
        doReturn(new ByteArrayOutputStream()).when(this.defaultWriter).write(any(), any(), any());

        this.defaultExceptionHandler = mock(ExceptionHandler.class);
        this.defaultPreparer = mock(ResponsePreparer.class);
        this.defaultHost = mock(Host.class);
        doReturn("default.org").when(this.defaultHost).getName();
        doReturn(this.defaultRouter).when(this.defaultHost).getRouter();
        doReturn(this.defaultExceptionHandler).when(this.defaultHost).getExceptionHandler();
        doReturn(this.defaultPreparer).when(this.defaultHost).getResponsePreparer();
        doReturn(this.defaultWriter).when(this.defaultHost).getResponseWriter();

        this.namedRouter = mock(Router.class);
        doReturn(new EmptyResponse()).when(this.namedRouter).handle(any());

        this.namedWriter = mock(ResponseWriter.class);
        doReturn(new ByteArrayOutputStream()).when(this.namedWriter).write(any(), any(), any());

        this.namedExceptionHandler = mock(ExceptionHandler.class);
        this.namedPreparer = mock(ResponsePreparer.class);
        this.namedHost = mock(Host.class);
        doReturn("example.org").when(this.namedHost).getName();
        doReturn(this.namedRouter).when(this.namedHost).getRouter();
        doReturn(this.namedExceptionHandler).when(this.namedHost).getExceptionHandler();
        doReturn(this.namedPreparer).when(this.namedHost).getResponsePreparer();
        doReturn(this.namedWriter).when(this.namedHost).getResponseWriter();

        this.handler.setDefaultHost(this.defaultHost);
        this.handler.addHost(this.namedHost);

        this.address = InetAddress.getLoopbackAddress();
        this.port = 0;
        this.out = new ByteArrayOutputStream();
        this.in = new ByteArrayInputStream(new byte[] {});
    }

    @ParameterizedTest
    @ValueSource(strings = {"http", "https"})
    public void testHttp11SequenceClosing(final String scheme) throws Exception {
        final MutableRequest first = this.buildRequest("example.org", 1);
        final MutableRequest second = this.buildRequest("example.org", 1);
        second.getHeaders().computeIfAbsent("connection", k -> new ArrayList<>()).add("final");
        final MutableRequest last = this.buildRequest("example.org", 1);
        last.getHeaders().computeIfAbsent("connection", k -> new ArrayList<>()).add("close");

        final Stack<MutableRequest> requests = new Stack<>();
        requests.push(last);
        requests.push(second);
        requests.push(first);

        final Stack<MutableRequest> handledRequests = new Stack<>();

        doAnswer(invocation -> {
                final MutableRequest request = requests.pop();
                handledRequests.push(request);
                return request;
        }).when(this.parser).parse(any());

        doAnswer(invocation -> {
            final MutableRequest request = invocation.getArgument(0);
            assertThat(request).isSameAs(handledRequests.peek());

            return new ByteArrayOutputStream();
        }).when(this.namedWriter).write(any(), any(), any());

        assertTimeout(Duration.ofSeconds(1), () ->
                this.handler.handle(
                        new Connection(this.address, this.port, this.in, this.out, scheme)
                )
        );

        assertThat(requests).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"http", "https"})
    public void testHttp10SequenceClosing(final String scheme) throws Exception {
        final MutableRequest first = this.buildRequest("example.org", 0);
        first.getHeaders().computeIfAbsent("connection", k -> new ArrayList<>()).add("keep-alive");
        final MutableRequest second = this.buildRequest("example.org", 0);
        second.getHeaders().computeIfAbsent("connection", k -> new ArrayList<>()).add("keep-alive");
        final MutableRequest last = this.buildRequest("example.org", 0);

        final Stack<MutableRequest> requests = new Stack<>();
        requests.push(last);
        requests.push(second);
        requests.push(first);

        final Stack<MutableRequest> handledRequests = new Stack<>();

        doAnswer(invocation -> {
            final MutableRequest request = requests.pop();
            handledRequests.push(request);
            return request;
        }).when(this.parser).parse(any());

        doAnswer(invocation -> {
            final MutableRequest request = invocation.getArgument(0);
            assertThat(request).isSameAs(handledRequests.peek());

            return new ByteArrayOutputStream();
        }).when(this.namedWriter).write(any(), any(), any());

        assertTimeout(Duration.ofSeconds(1), () ->
                this.handler.handle(
                        new Connection(this.address, this.port, this.in, this.out, scheme)
                )
        );

        assertThat(requests).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"http", "https"})
    public void testBadRequest(final String scheme) throws Exception {
        final HttpException exception = new BadRequestException();
        doThrow(exception).when(this.parser).parse(any());

        final Response exceptionResponse = new EmptyResponse();
        doReturn(exceptionResponse).when(this.defaultExceptionHandler)
                .handle(eq(null), eq(exception));

        assertTimeout(Duration.ofSeconds(1), () ->
                this.handler.handle(
                        new Connection(this.address, this.port, this.in, this.out, scheme)
                )
        );

        verify(this.defaultPreparer).prepare(eq(null), eq(exceptionResponse));
    }

    private MutableRequest buildRequest(final String host, final int minor) {
        final MutableRequest request = new MutableRequest(Method.GET, "/", 1, minor);
        request.getHeaders().computeIfAbsent("host", k -> new ArrayList<>()).add(host);
        return request;
    }
}
