package net.wukl.cacofony.server.host;

import net.wukl.cacofony.exception.ExceptionHandler;
import net.wukl.cacofony.http.request.MutableRequest;
import net.wukl.cacofony.http.response.Response;
import net.wukl.cacofony.http.response.ResponseCode;
import net.wukl.cacofony.mime.MimeParser;
import net.wukl.cacofony.route.Router;
import net.wukl.cacofony.route.RoutingEntry;
import net.wukl.cacofony.server.ServerSettings;
import net.wukl.cacodi.DependencyResolver;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * @author Luc Everse
 */
public class DefaultHostBuilderTest {
    @Test
    public void test() throws Exception {
        final DependencyResolver resolver = new DependencyResolver();

        final Router router = mock(Router.class);

        resolver.add(MimeParser.class, mock(MimeParser.class));
        resolver.add(ExceptionHandler.class, mock(ExceptionHandler.class));
        resolver.add(ServerSettings.class, mock(ServerSettings.class));
        resolver.add(Router.class, router);

        final List<RoutingEntry> addedEntries = new ArrayList<>();

        doAnswer(invocation -> addedEntries.add(invocation.getArgument(0)))
                .when(router).addRoute(any());

        new DefaultHostBuilder("default", resolver).build();

        assertThat(addedEntries).hasSize(1);

        final RoutingEntry entry = addedEntries.get(0);

        final Response response = (Response) entry.invoke(new MutableRequest());

        assertThat(response.getStatus()).as("status").isEqualTo(ResponseCode.NOT_FOUND);
    }
}
