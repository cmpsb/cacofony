package net.cmpsb.cacofony.http.cookie;

import net.cmpsb.cacofony.util.Ob;
import net.cmpsb.cacofony.util.UrlCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Luc Everse
 */
public class SimpleCookieParserTest {
    private CookieParser parser;

    @BeforeEach
    public void before() {
        this.parser = new CookieParser(new UrlCodec());
    }

    @Test
    public void testSingleValued() {
        final String line = "style=new";
        final Map<String, List<Cookie>> cookies = this.parser.parseSimple(line);

        assertThat(cookies).isEqualTo(Collections.singletonMap("style",
            Collections.singletonList(new Cookie("style", "new"))
        ));
    }

    @Test
    public void testSingleInvalid() {
        final String line = "cookie";
        final Map<?, ?> cookies = this.parser.parseSimple(line);

        assertThat(cookies).hasSize(0);
    }

    @Test
    public void testMultiWithInvalid() {
        final String line = "first=1st; twice=2; invalid; first=2nd; next=done";
        final Map<String, List<Cookie>> cookies = this.parser.parseSimple(line);

        assertThat(cookies).isEqualTo(Ob.map(
           "first", Arrays.asList(
               new Cookie("first", "1st"),
               new Cookie("first", "2nd")
           ),
           "twice", Collections.singletonList(
               new Cookie("twice", "2")
           ),
           "next", Collections.singletonList(
               new Cookie("next", "done")
           )
       ));
    }

    @Test
    public void testMultiWithTrailingInvalid() {
        final String line = "first=1st; twice=2; invalid; first=2nd; next=done; invalid";
        final Map<String, List<Cookie>> cookies = this.parser.parseSimple(line);

        assertThat(cookies).isEqualTo(Ob.map(
            "first", Arrays.asList(
                    new Cookie("first", "1st"),
                    new Cookie("first", "2nd")
            ),
            "twice", Collections.singletonList(
                    new Cookie("twice", "2")
            ),
            "next", Collections.singletonList(
                    new Cookie("next", "done")
            )
        ));
    }

    @Test
    public void testNullLine() {
        final Map<?, ?> nullMap = this.parser.parseSimple(null);
        assertThat(nullMap).isEmpty();
    }

    @Test
    public void testEmptyLine() {
        final Map<?, ?> emptyMap = this.parser.parseSimple("");
        assertThat(emptyMap).isEmpty();
    }
}
