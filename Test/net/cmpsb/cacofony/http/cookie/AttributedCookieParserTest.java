package net.cmpsb.cacofony.http.cookie;

import net.cmpsb.cacofony.util.UrlCodec;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author Luc Everse
 */
public class AttributedCookieParserTest {
    private CookieParser parser;

    @Before
    public void before() {
        this.parser = new CookieParser(new UrlCodec());
    }

    @Test(expected = InvalidCookieException.class)
    public void testSimpleValueless() {
        final String line = "name";
        this.parser.parseAttributed(line);
    }

    @Test(expected = InvalidCookieException.class)
    public void testSimpleEmpty() {
        this.parser.parseAttributed("");
    }

    @Test(expected = InvalidCookieException.class)
    public void testAttributedValueless() {
        this.parser.parseAttributed("name; Expires=right now");
    }

    @Test
    public void testSimpleValued() {
        final String line = "foo=bar";
        final Cookie cookie = this.parser.parseAttributed(line);

        assertThat("The name is correct.",
                   cookie.getName(),
                   is(equalTo("foo")));

        assertThat("The value is correct.",
                   cookie.getValue(),
                   is(equalTo("bar")));
    }

    @Test
    public void testWithAttributes() {
        final String line = "token=iIIIiIILiIlIlIIIliIIliIlIIL; Expires=now; Path = /; Secure";
        final Cookie cookie = this.parser.parseAttributed(line);

        assertThat("The name is correct.",
                   cookie.getName(),
                   is(equalTo("token")));

        assertThat("The value is correct.",
                   cookie.getValue(),
                   is(equalTo("iIIIiIILiIlIlIIIliIIliIlIIL")));

        assertThat("The Expires attribute is correct.",
                   cookie.getAttributes().get("expires"),
                   is(equalTo("now")));

        assertThat("The Path attribute is correct.",
                   cookie.getAttributes().get("path"),
                   is(equalTo("/")));

        assertThat("There is a Secure attribute.",
                   cookie.getAttributes().containsKey("secure"),
                   is(true));
    }

    @Test
    public void testMoreWithAttributes() {
        final String line = "baz=qux; Secure; HttpOnly";
        final Cookie cookie = this.parser.parseAttributed(line);

        assertThat("The name is correct.",
                   cookie.getName(),
                   is(equalTo("baz")));

        assertThat("The value is correct.",
                   cookie.getValue(),
                   is(equalTo("qux")));

        assertThat("There is a Secure attribute.",
                   cookie.getAttributes().containsKey("secure"),
                   is(true));

        assertThat("There is an HttpOnly attribute.",
                   cookie.getAttributes().containsKey("httponly"),
                   is(true));
    }

    @Test
    public void testEvenMoreWithAttributes() {
        final String line = "baz=qux; Secure; Expires=now";
        final Cookie cookie = this.parser.parseAttributed(line);

        assertThat("The name is correct.",
                cookie.getName(),
                is(equalTo("baz")));

        assertThat("The value is correct.",
                cookie.getValue(),
                is(equalTo("qux")));

        assertThat("There is a Secure attribute.",
                cookie.getAttributes().containsKey("secure"),
                is(true));

        assertThat("The Expires attribute is correct.",
                cookie.getAttributes().get("expires"),
                is(equalTo("now")));
    }
}
