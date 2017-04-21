package net.cmpsb.cacofony.http.cookie;

import net.cmpsb.cacofony.util.UrlCodec;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.is;

/**
 * @author Luc Everse
 */
public class AttributedCookieWriterTest {
    private CookieWriter cookieWriter;

    @Before
    public void before() {
        this.cookieWriter = new CookieWriter(new UrlCodec());
    }

    @Test
    public void testWriteValued() {
        final Cookie cookie = new Cookie("foo", "bar and baz");
        cookie.setHttpOnly();
        cookie.setDomain("example.com");
        cookie.setPath("/home");

        final String written = this.cookieWriter.writeAttributed(cookie);

        assertThat("The string starts with the key-value pair.",
                   written,
                   startsWith("foo=bar%20and%20baz"));

        assertThat("The string is as expected.",
                   written,
                   is(anyOf(
                       equalTo("foo=bar%20and%20baz; Path=/home; HttpOnly; Domain=example.com"),
                       equalTo("foo=bar%20and%20baz; Path=/home; Domain=example.com; HttpOnly"),
                       equalTo("foo=bar%20and%20baz; HttpOnly; Domain=example.com; Path=/home"),
                       equalTo("foo=bar%20and%20baz; HttpOnly; Path=/home; Domain=example.com"),
                       equalTo("foo=bar%20and%20baz; Domain=example.com; HttpOnly; Path=/home"),
                       equalTo("foo=bar%20and%20baz; Domain=example.com; Path=/home; HttpOnly")
                   ))
        );
    }
}
