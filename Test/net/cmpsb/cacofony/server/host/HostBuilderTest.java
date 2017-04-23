package net.cmpsb.cacofony.server.host;

import net.cmpsb.cacofony.di.DependencyResolver;
import net.cmpsb.cacofony.exception.ExceptionHandler;
import net.cmpsb.cacofony.mime.MimeParser;
import net.cmpsb.cacofony.route.Router;
import net.cmpsb.cacofony.server.ServerSettings;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

/**
 * @author Luc Everse
 */
public class HostBuilderTest {
    @Test
    public void testDependencyInheritance() {
        final String str = "test string!";
        final DependencyResolver resolver = new DependencyResolver();
        resolver.add(String.class, str);
        resolver.add(Router.class, mock(Router.class));
        resolver.add(MimeParser.class, mock(MimeParser.class));
        resolver.add(ExceptionHandler.class, mock(ExceptionHandler.class));
        resolver.add(ServerSettings.class, mock(ServerSettings.class));

        final HostBuilder builder = new HostBuilder("name", resolver);
        final Host host = builder.build();

        assertThat("The host inherits the dependencies from the server.",
                   host.getResolver().get(String.class),
                   is(str));
    }
}
