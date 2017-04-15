package net.cmpsb.cacofony.http.request;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the form parser.
 *
 * @author Luc Everse
 */
public class FormParserTest {
    private FormParser parser;

    @Before
    public void before() {
        this.parser = new FormParser();
    }

    @Test
    public void testSingle() {
        final String line = "key=value";

        final Map<String, List<String>> form = this.parser.parse(line);

        assertThat("There is one entry.",
                   form.size(),
                   is(1));

        assertThat("The key entry is correct.",
                   form.get("key"),
                   is(equalTo(Collections.singletonList("value"))));
    }

    @Test
    public void testMulti() {
        final String line = "key=one&key=two&three=four&key=three&test=yes";

        final Map<String, List<String>> form = this.parser.parse(line);

        assertThat("There are three entries.",
                   form.size(),
                   is(3));

        assertThat("The key entry is correct.",
                   form.get("key"),
                   is(equalTo(Arrays.asList("one", "two", "three"))));

        assertThat("The three entry is correct.",
                   form.get("three"),
                   is(equalTo(Collections.singletonList("four"))));

        assertThat("The test entry is correct.",
                   form.get("test"),
                   is(equalTo(Collections.singletonList("yes"))));
    }

    @Test
    public void testValueless() {
        final String line = "title&encode=";

        final Map<String, List<String>> form = this.parser.parse(line);

        assertThat("There are two entries.",
                   form.size(),
                   is(2));

        assertThat("The title entry is an empty string.",
                   form.get("title"),
                   is(equalTo(Collections.singletonList(""))));

        assertThat("The encode entry is an empty string.",
                   form.get("encode"),
                   is(equalTo(Collections.singletonList(""))));
    }
}
