package net.cmpsb.cacofony.http.request;

import net.cmpsb.cacofony.http.exception.HttpException;
import net.cmpsb.cacofony.http.exception.NotImplementedException;
import net.cmpsb.cacofony.io.HttpInputStream;
import net.cmpsb.cacofony.io.StreamHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author Luc Everse
 */
public class RequestParserTest {
    private RequestParser parser;

    @Before
    public void before() {
        final HeaderParser headerParser = new HeaderParser();
        final StreamHelper streamHelper = new StreamHelper();
        this.parser = new RequestParser(headerParser, streamHelper);
    }

    @Test
    public void testNormalRequest() throws IOException {
        final String packet =
            "GET /index.html HTTP/1.1\r\n"
          + "Host: cmpsb.net\r\n"
          + "Content-Length: 8\r\n"
          + "\r\n"
          + "14616742";

        final HttpInputStream in = this.getStream(packet);
        final MutableRequest request = this.parser.parse(in);

        final byte[] payload = new byte[8];
        final int length = request.getBody().read(payload);
        final String body = new String(payload, StandardCharsets.ISO_8859_1);

        assertThat("The method is correct.",
                   request.getMethod(),
                   is(equalTo(Method.GET)));

        assertThat("The path is correct.",
                   request.getRawPath(),
                   is(equalTo("/index.html")));

        assertThat("The content length is correct.",
                   length,
                   is(8));

        assertThat("The body length is correct.",
                   body,
                   is(equalTo("14616742")));
    }

    @Test
    public void testChunkedRequest() throws IOException {
        final String packet =
            "POST /login/process HTTP/1.1\r\n"
          + "Host: cmpsb.net\r\n"
          + "Transfer-Encoding: chunked\r\n"
          + "\r\n"
          + "4\r\n"
          + "1461"
          + "\r\n"
          + "4\r\n"
          + "6742"
          + "\r\n"
          + "0"
          + "\r\n"
          + "\r\n";

        final HttpInputStream in = this.getStream(packet);
        final MutableRequest request = this.parser.parse(in);

        final byte[] payload = new byte[8];
        final int length = request.getBody().read(payload);
        final String body = new String(payload, StandardCharsets.ISO_8859_1);

        assertThat("The method is correct.",
                request.getMethod(),
                is(equalTo(Method.POST)));

        assertThat("The path is correct.",
                request.getRawPath(),
                is(equalTo("/login/process")));

        assertThat("The content length is correct.",
                length,
                is(8));

        assertThat("The body length is correct.",
                body,
                is(equalTo("14616742")));
    }

    @Test(expected = NotImplementedException.class)
    public void testUnknownEncoding() throws IOException {
        final String packet = "HEAD / HTTP/1.1\r\nTransfer-Encoding: _invalid\r\n\r\n";
        final HttpInputStream in = this.getStream(packet);
        this.parser.parse(in);
    }

    @Test(expected = HttpException.class)
    public void testBadRequestLine() throws IOException {
        final String packet = "GET /";
        final HttpInputStream in = this.getStream(packet);
        this.parser.parse(in);
    }

    @Test(expected = HttpException.class)
    public void testUnknownMethod() throws IOException {
        final String packet = "FROBNICATE / HTTP/1.1\r\n\r\n";
        final HttpInputStream in = this.getStream(packet);
        this.parser.parse(in);
    }

    @Test(expected = HttpException.class)
    public void testBadVersion() throws IOException {
        final String packet = "GET / HTTPbis\r\n\r\n";
        final HttpInputStream in = this.getStream(packet);
        this.parser.parse(in);
    }

    @Test(expected = HttpException.class)
    public void testFutureVersion() throws IOException {
        final String packet = "GET / HTTP/99.99";
        final HttpInputStream in = this.getStream(packet);
        this.parser.parse(in);
    }

    private HttpInputStream getStream(final String packet) {
        final byte[] bytes = packet.getBytes(StandardCharsets.ISO_8859_1);
        final ByteArrayInputStream source = new ByteArrayInputStream(bytes);
        return new HttpInputStream(source);
    }
}
