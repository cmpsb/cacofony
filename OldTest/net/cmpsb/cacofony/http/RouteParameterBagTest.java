package net.cmpsb.cacofony.http;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the route parameter bag.
 *
 * @author Luc Everse
 */
public class RouteParameterBagTest extends ParameterBagTest<RouteParameterBag> {
    @Before
    public void before() {
        super.before();

        final Map<String, String> parameters = new HashMap<>();

        for (final String param : this.requiredParameters.keySet()) {
            final String[] values = this.requiredParameters.get(param);

            if (values.length == 0) {
                continue;
            }

            parameters.put(param, values[0]);
        }

        this.bag = new RouteParameterBag(parameters);
    }

    @Test
    public void testGetAll() {
        final List<String> values = this.bag.getAll("one");

        assertThat("Retrieving all values maps to a singleton list containing that value.",
                   values,
                   is(equalTo(Collections.singletonList("1"))));
    }
}
