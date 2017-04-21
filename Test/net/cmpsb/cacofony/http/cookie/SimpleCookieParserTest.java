package net.cmpsb.cacofony.http.cookie;

import net.cmpsb.cacofony.util.Ob;
import net.cmpsb.cacofony.util.UrlCodec;
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
 * @author Luc Everse
 */
public class SimpleCookieParserTest {
    private CookieParser parser;

    @Before
    public void before() {
        this.parser = new CookieParser(new UrlCodec());
    }

    @Test
    public void testSingleValued() {
        final String line = "style=new";
        final Map<String, List<Cookie>> cookies = this.parser.parseSimple(line);

        assertThat("The result is as expected.",
                   cookies,
                   is(equalTo(
                       Collections.singletonMap("style",
                           Collections.singletonList(new Cookie("style", "new"))
                       )
                   ))
        );
    }

    @Test
    public void testSingleInvalid() {
        final String line = "cookie";
        final Map<?, ?> cookies = this.parser.parseSimple(line);

        assertThat("The map is empty.",
                   cookies.isEmpty(),
                   is(true));
    }

    @Test
    public void testMultiWithInvalid() {
        final String line = "first=1st; twice=2; invalid; first=2nd; next=done";
        final Map<String, List<Cookie>> cookies = this.parser.parseSimple(line);

        assertThat("The result is as expected.",
                   cookies,
                   is(equalTo(Ob.map(
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
                   )))
        );
    }

    @Test
    public void testMultiWithTrailingInvalid() {
        final String line = "first=1st; twice=2; invalid; first=2nd; next=done; invalid";
        final Map<String, List<Cookie>> cookies = this.parser.parseSimple(line);

        assertThat("The result is as expected.",
                cookies,
                is(equalTo(Ob.map(
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
                )))
        );
    }

    @Test
    public void testEmptyLine() {
        final Map<?, ?> nullMap = this.parser.parseSimple(null);
        assertThat("The map is empty for a null header.",
                   nullMap.isEmpty(),
                   is(true));

        final Map<?, ?> emptyMap = this.parser.parseSimple("");
        assertThat("The map is empty for an empty header.",
                   emptyMap.isEmpty(),
                   is(true));
    }
}
