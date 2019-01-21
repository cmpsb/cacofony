package net.wukl.cacofony.http.request;

import net.wukl.cacofony.util.Ob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the form parser.
 *
 * @author Luc Everse
 */
public class FormParserTest {
    private FormParser parser;

    @BeforeEach
    public void before() {
        this.parser = new FormParser();
    }

    @Test
    public void testSingle() {
        final String line = "key=value";

        final Map<String, List<String>> form = this.parser.parse(line);

        assertThat(form).isEqualTo(Collections.singletonMap("key",
            Collections.singletonList("value")
        ));
    }

    @Test
    public void testMulti() {
        final String line = "key=one&key=two&three=four&key=three&test=yes";

        final Map<String, List<String>> form = this.parser.parse(line);

        assertThat(form).isEqualTo(Ob.map(
                "key", Arrays.asList("one", "two", "three"),
                "three", Collections.singletonList("four"),
                "test", Collections.singletonList("yes")
        ));
    }

    @Test
    public void testValueless() {
        final String line = "title&encode=";

        final Map<String, List<String>> form = this.parser.parse(line);

        assertThat(form).isEqualTo(Ob.map(
                "title", Collections.singletonList(""),
                "encode", Collections.singletonList("")
        ));
    }
}
