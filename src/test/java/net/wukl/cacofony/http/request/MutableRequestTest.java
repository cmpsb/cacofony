package net.wukl.cacofony.http.request;

import net.wukl.cacofony.http.cookie.Cookie;
import net.wukl.cacofony.http.exception.BadRequestException;
import net.wukl.cacofony.http.exception.HttpException;
import net.wukl.cacofony.util.Ob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Luc Everse
 */
public class MutableRequestTest {
    private MutableRequest request;

    private String defaultInput;
    private byte[] defaultPacket;

    @BeforeEach
    public void before() {
        this.request = new MutableRequest(Method.GET, "/path/", 1, 0);

        this.defaultInput = "Test string for testing!";
        this.defaultPacket = this.defaultInput.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    public void testReadFullStaticBody() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(this.defaultPacket);

        this.request.setBody(in);
        this.request.setContentLength(this.defaultInput.length());

        final String output = this.request.readFullBody(8192, StandardCharsets.UTF_8);

        assertThat(output).isEqualTo(this.defaultInput);
    }

    @Test
    public void testReadEmptyStaticRequest() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(new byte[] {});

        this.request.setBody(in);
        this.request.setContentLength(0);

        final String output = this.request.readFullBody(8192, StandardCharsets.UTF_8);

        assertThat(output).isEmpty();
    }

    @Test
    public void testGetHost() {
        this.request.getHeaders().put("host", Collections.singletonList("example.org"));

        assertThat(this.request.getHost()).as("host").isEqualTo("example.org");
    }

    @Test
    public void testMultipleHostHeaders() {
        this.request.getHeaders().put("host", Arrays.asList("example.com", "example.net"));

        assertThrows(BadRequestException.class, this.request::getHost);
    }

    @Test
    public void testNoHostHeader() {
        assertThrows(BadRequestException.class, this.request::getHost);
    }

    @Test
    public void testReadStaticTooLong() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(this.defaultPacket);

        this.request.setBody(in);
        this.request.setContentLength(this.defaultPacket.length);

        assertThrows(HttpException.class, () ->
                this.request.readFullBody(4, StandardCharsets.UTF_8)
        );
    }

    @Test
    public void testReadFullEncodedBody() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(this.defaultPacket);

        this.request.setBody(in);
        this.request.setContentLength(-1);

        final String output = this.request.readFullBody(8192, StandardCharsets.UTF_8);

        assertThat(output).isEqualTo(this.defaultInput);
    }

    @Test
    public void testReadEncodedTooLong() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(this.defaultPacket);

        this.request.setBody(in);
        this.request.setContentLength(-1);

        assertThrows(HttpException.class, () ->
                this.request.readFullBody(4, StandardCharsets.UTF_8)
        );
    }

    @Test
    public void testGetCookie() {
        final Cookie cookie = new Cookie("cookie", "value");

        this.request.setCookies(
            Collections.singletonMap(cookie.getName(),
                Collections.singletonList(cookie)
            )
        );

        assertThat(this.request.getCookie(cookie.getName())).as("cookie").isEqualTo(cookie);
    }

    @Test
    public void testGetNonexistentCookie() {
        final Cookie cookie = new Cookie("cookie", "value");

        this.request.setCookies(
                Collections.singletonMap(cookie.getName(),
                        Collections.singletonList(cookie)
                )
        );

        assertThat(this.request.getCookie("not the right cookie")).as("nonexistent cookie")
                .isNull();
    }

    @Test
    public void testGetDefaultQueryParam() {
        this.request.setQueryParameters(Ob.map(
            "aaa", "aAa",
            "bbb", "bBb"
        ));

        final String value = this.request.getQueryParameter("ccc", "CcC");

        assertThat(value).as("default").isEqualTo("CcC");
    }

    @Test
    public void testGetQueryParam() {
        this.request.setQueryParameters(Ob.map(
                "aaa", "aAa",
                "bbb", "bBb"
        ));

        final String value = this.request.getQueryParameter("aaa", "AaA");

        assertThat(value).as("given").isEqualTo("aAa");
    }

    @Test
    public void testGetDefaultPathParam() {
        this.request.setPathParameters(Ob.map(
                "one", "ONE",
                "TWO", "two"
        ));

        final String value = this.request.getPathParameter("three", "three");

        assertThat(value).as("default").isEqualTo("three");
    }

    @Test
    public void testGetPathParam() {
        this.request.setPathParameters(Ob.map(
                "one", "ONE",
                "TWO", "two"
        ));

        final String value = this.request.getPathParameter("one", "one");

        assertThat(value).as("given").isEqualTo("ONE");
    }
}
