package net.cmpsb.cacofony.http.request;

import net.cmpsb.cacofony.http.cookie.Cookie;
import net.cmpsb.cacofony.http.exception.BadRequestException;
import net.cmpsb.cacofony.http.exception.HttpException;
import net.cmpsb.cacofony.util.Ob;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.nullValue;

/**
 * @author Luc Everse
 */
public class MutableRequestTest {
    private MutableRequest request;

    private String defaultInput;
    private byte[] defaultPacket;

    @Before
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

        assertThat("The input and output are equal.",
                   output,
                   is(equalTo(this.defaultInput)));
    }

    @Test
    public void testReadEmptyStaticRequest() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(new byte[] {});

        this.request.setBody(in);
        this.request.setContentLength(0);

        final String output = this.request.readFullBody(8192, StandardCharsets.UTF_8);

        assertThat("The output is an empty string.",
                   output,
                   isEmptyOrNullString());
    }

    @Test
    public void testGetHost() {
        this.request.getHeaders().put("host", Collections.singletonList("example.org"));

        assertThat("The returned host is the value of the single Host header.",
                   this.request.getHost(),
                   is(equalTo("example.org")));
    }

    @Test(expected = BadRequestException.class)
    public void testMultipleHostHeaders() {
        this.request.getHeaders().put("host", Arrays.asList("example.com", "example.net"));
        this.request.getHost();
    }

    @Test(expected = BadRequestException.class)
    public void testNoHostHeader() {
        this.request.getHost();
    }

    @Test(expected = HttpException.class)
    public void testReadStaticTooLong() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(this.defaultPacket);

        this.request.setBody(in);
        this.request.setContentLength(this.defaultPacket.length);

        this.request.readFullBody(4, StandardCharsets.UTF_8);
    }

    @Test
    public void testReadFullEncodedBody() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(this.defaultPacket);

        this.request.setBody(in);
        this.request.setContentLength(-1);

        final String output = this.request.readFullBody(8192, StandardCharsets.UTF_8);

        assertThat("The input and output are equal.",
                   output,
                   is(equalTo(this.defaultInput)));
    }

    @Test(expected = HttpException.class)
    public void testReadEncodedTooLong() throws IOException {
        final ByteArrayInputStream in = new ByteArrayInputStream(this.defaultPacket);

        this.request.setBody(in);
        this.request.setContentLength(-1);

        this.request.readFullBody(4, StandardCharsets.UTF_8);
    }

    @Test
    public void testGetCookie() {
        final Cookie cookie = new Cookie("cookie", "value");

        this.request.setCookies(
            Collections.singletonMap(cookie.getName(),
                Collections.singletonList(cookie)
            )
        );

        assertThat("The returned cookie is as expected.",
                   this.request.getCookie(cookie.getName()),
                   is(equalTo(cookie)));
    }

    @Test
    public void testGetNonexistentCookie() {
        final Cookie cookie = new Cookie("cookie", "value");

        this.request.setCookies(
                Collections.singletonMap(cookie.getName(),
                        Collections.singletonList(cookie)
                )
        );

        assertThat("The returned cookie is as expected.",
                   this.request.getCookie("not the right cookie"),
                   is(nullValue()));
    }

    @Test
    public void testGetDefaultQueryParam() {
        this.request.setQueryParameters(Ob.map(
            "aaa", "aAa",
            "bbb", "bBb"
        ));

        final String value = this.request.getQueryParameter("ccc", "CcC");

        assertThat("The returned value is the default value.",
                   value,
                   is(equalTo("CcC")));
    }

    @Test
    public void testGetQueryParam() {
        this.request.setQueryParameters(Ob.map(
                "aaa", "aAa",
                "bbb", "bBb"
        ));

        final String value = this.request.getQueryParameter("aaa", "AaA");

        assertThat("The returned value is the stored value.",
                   value,
                   is(equalTo("aAa")));
    }

    @Test
    public void testGetDefaultPathParam() {
        this.request.setPathParameters(Ob.map(
                "one", "ONE",
                "TWO", "two"
        ));

        final String value = this.request.getPathParameter("three", "three");

        assertThat("The returned value is the default value.",
                value,
                is(equalTo("three")));
    }

    @Test
    public void testGetPathParam() {
        this.request.setPathParameters(Ob.map(
                "one", "ONE",
                "TWO", "two"
        ));

        final String value = this.request.getPathParameter("one", "one");

        assertThat("The returned value is the stored value.",
                value,
                is(equalTo("ONE")));
    }
}
