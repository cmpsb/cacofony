package net.cmpsb.cacofony.routing;

import net.cmpsb.cacofony.http.Method;
import net.cmpsb.cacofony.response.Response;
import net.cmpsb.cacofony.response.ResponseTransformer;
import net.cmpsb.cacofony.util.EnumerationHelpers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the router.
 * These tests are really ugly. Beware.
 *
 * @author Luc Everse
 */
public class RouterTest {
    private Router router;
    private org.eclipse.jetty.server.Request jettyRequest;
    private MockHttpServletResponse response;

    @Before
    public void before() throws IOException {
        this.jettyRequest = new org.eclipse.jetty.server.Request();

        this.response = new MockHttpServletResponse();

        final StringWriter target = new StringWriter();
        when(this.response.getWriter()).thenReturn(new PrintWriter(target));

        this.router = new Router();

        final CompiledPath rootPath =
                new CompiledPath("/", Pattern.compile("/"), Collections.emptyList());
        final RouteAction rootAction = (req) -> "root";
        final VerificationTransformer rootTransformer = new VerificationTransformer("root");
        final RoutingEntry rootEntry = new RoutingEntry(
                "root", rootPath, rootAction, rootTransformer, Collections.singletonList(Method.GET)
        );

        this.router.addRoute(rootEntry);
    }

    @Test
    public void testRoot() throws IOException, ServletException {
        when(jettyRequest.getMethod()).thenReturn("GET");
        when(jettyRequest.getHeaders("Accept")).thenReturn(
                EnumerationHelpers.wrap(
                        Arrays.asList("text/plain", "unrelated/mime-type, */*;q=0.8")
                )
        );

        this.router.handle("/", this.jettyRequest, this.jettyRequest, this.response);

        verify(this.response, times(1)).setStatus(200);
        verify(this.response, times(1)).setContentType("*/*");
    }

    @Test
    public void testBadMethod() throws IOException, ServletException {
        when(jettyRequest.getMethod()).thenReturn("TOTAL COCK SALAD");

        this.router.handle("/", this.jettyRequest, this.jettyRequest, this.response);

        verify(this.response, times(1)).setStatus(400);
    }

    @Test
    public void testUnacceptable() throws IOException, ServletException {
        when(jettyRequest.getMethod()).thenReturn("GET");
        when(jettyRequest.getHeaders("Accept")).thenReturn(
                EnumerationHelpers.wrap(
                        Arrays.asList("image/jpeg", "image/webp")
                )
        );

        this.router.handle("/", this.jettyRequest, this.jettyRequest, this.response);

        verify(this.response, times(1)).setStatus(404);
    }

    @Test
    public void testResponseObject() throws IOException, ServletException {
        final CompiledPath path =
                new CompiledPath("/test", Pattern.compile("/test/?"), Collections.emptyList());
        final RouteAction action = (req) -> new Response<>(201, "test");
        final VerificationTransformer transformer = new VerificationTransformer("test");
        final RoutingEntry rootEntry = new RoutingEntry(
                "test", path, action, transformer, Collections.singletonList(Method.GET)
        );

        this.router.addRoute(rootEntry);

        when(jettyRequest.getMethod()).thenReturn("GET");
        when(jettyRequest.getHeaders("Accept")).thenReturn(
                EnumerationHelpers.wrap(
                        Arrays.asList("text/plain", "unrelated/mime-type, */*;q=0.8")
                )
        );

        this.router.handle("/test", this.jettyRequest, this.jettyRequest, this.response);

        verify(this.response, times(1)).setStatus(201);
    }

    private class VerificationTransformer implements ResponseTransformer {
        private final Object expect;

        VerificationTransformer(final Object expect) {
            this.expect = expect;
        }

        /**
         * Transform a response.
         *
         * @param object the response
         * @param writer where to write the response
         * @throws IOException if an I/O exception occurs in the print writer
         */
        @Override
        public void transform(final Object object, final PrintWriter writer) throws IOException {
            assertThat("The expected object is returned.",
                       object,
                       is(equalTo(this.expect)));
        }

        /**
         * Transform a complex response.
         *
         * @param response the complex response
         * @param writer   where to write the response
         * @throws IOException if an I/O exception occurs in the print writer
         */
        @Override
        public void transform(final Response response, final PrintWriter writer)
                throws IOException {
            this.transform(response.getData(), writer);
        }

        /**
         * @return the content types this response transformer may produce
         */
        @Override
        public List<String> getContentTypes() {
            return Arrays.asList("x-application/verification", "*/*");
        }
    }
}
