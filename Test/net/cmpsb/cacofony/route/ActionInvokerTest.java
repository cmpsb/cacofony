package net.cmpsb.cacofony.route;

import net.cmpsb.cacofony.controller.Controller;
import net.cmpsb.cacofony.controller.ControllerLoader;
import net.cmpsb.cacofony.http.request.MutableRequest;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.http.response.Response;
import net.cmpsb.cacofony.mime.FastMimeParser;
import net.wukl.cacodi.DependencyResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Luc Everse
 */
public class ActionInvokerTest {
    private InvocationTarget target;
    private ActionInvoker invoker;
    private DependencyResolver di;
    private MutableRequest request;
    private Map<String, RoutingEntry> entries;

    @BeforeEach
    public void before() {
        this.target = new InvocationTarget();

        this.di = Mockito.mock(DependencyResolver.class);
        Mockito.when(this.di.get(Mockito.eq(InvocationTarget.class))).thenReturn(this.target);

        this.invoker = new ActionInvoker(this.di);
        this.request = new MutableRequest(
                net.cmpsb.cacofony.http.request.Method.GET,
                "/path",
                0, 1
        );

        this.entries = new HashMap<>();
        final Router router = new Router(null, null, null) {
            @Override
            public void addRoute(final RoutingEntry entry) {
                ActionInvokerTest.this.entries.put(entry.getPath().getPath(), entry);
            }
        };

        final ControllerLoader loader =
                new ControllerLoader(this.di, router, new FastMimeParser(), new PathCompiler());
        loader.load("", InvocationTarget.class);
    }

    @Test
    public void testNoParameter() throws Exception {
        final RoutingEntry entry = this.entries.get("/noparam");

        this.invoker.invoke(entry, this.request);

        assertThat(this.target.invoked).isTrue();
    }

    @Test
    public void testRequestParameter() throws Exception {
        final RoutingEntry entry = this.entries.get("/reqparam");

        this.invoker.invoke(entry, this.request);

        assertThat(this.target.invoked).isTrue();
    }

    /**
     * The controller class the invoker should mess with.
     */
    public class InvocationTarget extends Controller {
        private boolean invoked = false;

        @Route(path = "/noparam")
        public Response noParameterAction() {
            this.invoked = true;

            return null;
        }

        @Route(path = "/reqparam")
        public Response requestParameterAction(final Request request) {
            this.invoked = true;

            assertThat(request).isNotNull();
            assertThat(request.getRawPath()).isEqualTo("/path");

            return null;
        }
    }
}
