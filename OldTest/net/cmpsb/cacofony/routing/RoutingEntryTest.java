package net.cmpsb.cacofony.routing;

import net.cmpsb.cacofony.http.Method;
import net.cmpsb.cacofony.response.RawResponseTransformer;
import net.cmpsb.cacofony.response.ResponseTransformer;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

/**
 * Tests for the routing entry model class.
 *
 * @author Luc Everse
 */
public class RoutingEntryTest {
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(RoutingEntry.class).verify();
    }

    @Test
    public void testConstructor() {
        final String name = "name";
        final CompiledPath path =  mock(CompiledPath.class);
        final RouteAction action = (request) -> null;
        final ResponseTransformer transformer = mock(RawResponseTransformer.class);
        final List<Method> methods = Arrays.asList(Method.CONNECT, Method.GET);

        final RoutingEntry entry = new RoutingEntry(name, path, action, transformer, methods);

        assertThat("The name is stored correctly.",
                   entry.getName(),
                   is(equalTo(name)));

        assertThat("The path is stored correctly.",
                   entry.getPath(),
                   is(equalTo(path)));

        assertThat("The action is stored correctly.",
                   entry.getAction(),
                   is(equalTo(action)));

        assertThat("The transformer is stored correctly.",
                   entry.getTransformer(),
                   is(equalTo(transformer)));

        assertThat("The method list is stored correctly.",
                   entry.getMethods(),
                   is(equalTo(methods)));
    }
}
