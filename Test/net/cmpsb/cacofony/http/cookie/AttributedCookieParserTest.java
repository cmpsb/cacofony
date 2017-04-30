package net.cmpsb.cacofony.http.cookie;

import net.cmpsb.cacofony.util.UrlCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Luc Everse
 */
public class AttributedCookieParserTest {
    private CookieParser parser;

    @BeforeEach
    public void before() {
        this.parser = new CookieParser(new UrlCodec());
    }

    @Test
    public void testSimpleValueless() {
        assertThrows(InvalidCookieException.class, () -> this.parser.parseAttributed("name"));
    }

    @Test
    public void testSimpleEmpty() {
        assertThrows(InvalidCookieException.class, () -> this.parser.parseAttributed(""));
    }

    @Test
    public void testAttributedValueless() {
        assertThrows(InvalidCookieException.class, () ->
            this.parser.parseAttributed("name; Expires=right now")
        );
    }

    @Test
    public void testSimpleValued() {
        final String line = "foo=bar";
        final Cookie cookie = this.parser.parseAttributed(line);

        assertThat(cookie.getName()).as("name").isEqualTo("foo");
        assertThat(cookie.getValue()).as("value").isEqualTo("bar");
    }

    @Test
    public void testWithAttributes() {
        final String line = "token=iIIIiIILiIlIliIIliIlIIL; Expires=now; Path = /; Secure";
        final Cookie cookie = this.parser.parseAttributed(line);

        assertThat(cookie.getName()).as("name").isEqualTo("token");
        assertThat(cookie.getValue()).as("value").isEqualTo("iIIIiIILiIlIliIIliIlIIL");
        assertThat(cookie.getAttributes()).as("attributes")
                .containsEntry("expires", "now")
                .containsEntry("path", "/")
                .containsKey("secure");
    }

    @Test
    public void testMoreWithAttributes() {
        final String line = "baz=qux; Secure; HttpOnly";
        final Cookie cookie = this.parser.parseAttributed(line);

        assertThat(cookie.getName()).as("name").isEqualTo("baz");
        assertThat(cookie.getValue()).as("value").isEqualTo("qux");
        assertThat(cookie.getAttributes()).as("attributes")
                .containsKeys("secure", "httponly");
    }

    @Test
    public void testEvenMoreWithAttributes() {
        final String line = "baz=qux; Secure; Expires=now";
        final Cookie cookie = this.parser.parseAttributed(line);

        assertThat(cookie.getName()).as("name").isEqualTo("baz");
        assertThat(cookie.getValue()).as("value").isEqualTo("qux");
        assertThat(cookie.getAttributes()).as("attributes")
                .containsKey("secure")
                .containsEntry("expires", "now");

    }
}
