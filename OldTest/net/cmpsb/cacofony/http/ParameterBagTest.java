package net.cmpsb.cacofony.http;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * A generic test for parameter bags.
 *
 * @author Luc Everse
 */
public abstract class ParameterBagTest<B extends ParameterBag> {
    public Map<String, String[]> requiredParameters;

    public B bag;

    @Before
    public void before() {
        this.requiredParameters = new HashMap<>();
        this.requiredParameters.put("one", new String[]{"1", "one"});
        this.requiredParameters.put("two", new String[]{"two", "2"});
        this.requiredParameters.put("three", new String[]{"333"});
        this.requiredParameters.put("empty", new String[]{""});
        this.requiredParameters.put("none", new String[]{});
    }

    @Test
    public void testGetString() {
        final String parameter = this.bag.get("one", "2");

        assertThat("An existing parameter returns the right value.",
                parameter,
                is(equalTo("1")));
    }

    @Test
    public void testGetStringNonexistentParameter() {
        final String parameter = this.bag.get("__nonexistent__", "value");

        assertThat("A nonexistent parameter returns the default value.",
                parameter,
                is(equalTo("value")));
    }

    @Test
    public void testGetStringEmptyValue() {
        final String parameter = this.bag.get("empty", "empty");

        assertThat("A parameter with an empty value returns the default value.",
                parameter,
                is(equalTo("empty")));
    }

    @Test
    public void testGetLong() {
        final long parameter = this.bag.get("three", -4);

        assertThat("An existing parameter returns the correct long value.",
                parameter,
                is(333L));
    }

    @Test
    public void testGetLongNonexistentParameter() {
        final long parameter = this.bag.get("__nonexistent_long__", 14);

        assertThat("A nonexistent parameter returns the default long value.",
                parameter,
                is(14L));
    }

    @Test
    public void testGetLongEmptyValue() {
        final long parameter = this.bag.get("empty", 616);

        assertThat("A parameter with an empty value returns the default long value.",
                parameter,
                is(616L));
    }

    @Test
    public void testGetLongNonNumericValue() {
        final long parameter = this.bag.get("two", 742);

        assertThat("A parameter value that's not parsable as a number returns the default value.",
                parameter,
                is(742L));
    }

    @Test
    public void testGetAllNonexistentParameter() {
        final List<String> values = this.bag.getAll("__nonexistent__");

        assertThat("Retrieving all nonexistent values returns a 0-length list.",
                values.size(),
                is(0));
    }

    @Test
    public void testHas() {
        assertThat("An existent parameter is reported as such.",
                this.bag.has("one"),
                is(true));

        assertThat("A nonexistent parameter is reported as such.",
                this.bag.has("__nonexistent__"),
                is(false));
    }
}
