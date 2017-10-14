package net.cmpsb.cacofony.http.cookie;

import net.cmpsb.cacofony.util.UrlCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Luc Everse
 */
public class AttributedCookieWriterTest {
    private CookieWriter cookieWriter;

    @BeforeEach
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

        assertThat(written).isIn(
            "foo=bar%20and%20baz; Path=/home; HttpOnly; Domain=example.com",
            "foo=bar%20and%20baz; Path=/home; Domain=example.com; HttpOnly",
            "foo=bar%20and%20baz; HttpOnly; Domain=example.com; Path=/home",
            "foo=bar%20and%20baz; HttpOnly; Path=/home; Domain=example.com",
            "foo=bar%20and%20baz; Domain=example.com; HttpOnly; Path=/home",
            "foo=bar%20and%20baz; Domain=example.com; Path=/home; HttpOnly"
        );
    }
}
