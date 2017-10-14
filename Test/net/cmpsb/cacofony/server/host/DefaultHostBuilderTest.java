package net.cmpsb.cacofony.server.host;

import net.cmpsb.cacofony.di.DependencyResolver;
import net.cmpsb.cacofony.exception.ExceptionHandler;
import net.cmpsb.cacofony.http.request.MutableRequest;
import net.cmpsb.cacofony.http.response.Response;
import net.cmpsb.cacofony.http.response.ResponseCode;
import net.cmpsb.cacofony.mime.MimeParser;
import net.cmpsb.cacofony.route.Router;
import net.cmpsb.cacofony.route.RoutingEntry;
import net.cmpsb.cacofony.server.ServerSettings;
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
