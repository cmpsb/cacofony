package net.wukl.cacofony.server.host;

import net.wukl.cacofony.exception.ExceptionHandler;
import net.wukl.cacofony.mime.MimeParser;
import net.wukl.cacofony.route.Router;
import net.wukl.cacofony.server.ServerSettings;
import net.wukl.cacodi.DependencyResolver;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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

        assertThat(host.getResolver().get(String.class)).isSameAs(str);
    }
}
