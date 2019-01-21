package net.wukl.cacofony.http.request;

import net.wukl.cacofony.http.exception.BadRequestException;
import net.wukl.cacofony.http.exception.HttpException;
import net.wukl.cacofony.http.exception.NotImplementedException;
import net.wukl.cacofony.io.HttpInputStream;
import net.wukl.cacofony.io.StreamHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Luc Everse
 */
public class RequestParserTest {
    private RequestParser parser;

    @BeforeEach
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

        assertThat(request.getMethod()).as("method").isEqualTo(Method.GET);
        assertThat(request.getRawPath()).as("raw path").isEqualTo("/index.html");
        assertThat(length).as("body length").isEqualTo(8);
        assertThat(body).as("body").isEqualTo("14616742");
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

        assertThat(request.getMethod()).as("method").isEqualTo(Method.POST);
        assertThat(request.getRawPath()).as("raw path").isEqualTo("/login/process");
        assertThat(length).as("body length").isEqualTo(8);
        assertThat(body).as("body").isEqualTo("14616742");
    }

    @Test
    public void testBodylessRequest() throws IOException {
        final String packet =
            "GET / HTTP/1.1\r\n"
          + "Host: cmpsb.net\r\n"
          + "\r\n";

        final HttpInputStream in = this.getStream(packet);
        final MutableRequest request = this.parser.parse(in);

        assertThat(request.getContentLength()).as("body length").isEqualTo(0);
    }

    @Test
    public void testEncodedStaticRequest() throws IOException {
        final String packet =
            "GET / HTTP/1.1\r\n"
          + "Host: cmpsb.net\r\n"
          + "Transfer-Encoding: chunked\r\n"
          + "Content-Length: 22\r\n"
          + "\r\n";

        final HttpInputStream in = this.getStream(packet);
        assertThrows(BadRequestException.class, () -> this.parser.parse(in));
    }

    @Test
    public void testHttp09() throws IOException {
        final String packet = "GET / HTTP/0.9\r\nHost: cmpsb.net\r\n\r\n";

        final HttpInputStream in = this.getStream(packet);
        final MutableRequest request = this.parser.parse(in);

        assertThat(request.getMajorVersion()).as("major version").isEqualTo(0);
        assertThat(request.getMinorVersion()).as("minor version").isEqualTo(9);
    }

    @Test
    public void testEof() throws IOException {
        final String packet = "";
        final HttpInputStream in = this.getStream(packet);
        assertThrows(IOException.class, () -> this.parser.parse(in));
    }

    @Test
    public void testUnknownEncoding() throws IOException {
        final String packet = "HEAD / HTTP/1.1\r\nTransfer-Encoding: _invalid\r\n\r\n";
        final HttpInputStream in = this.getStream(packet);
        assertThrows(NotImplementedException.class, () -> this.parser.parse(in));
    }

    @Test
    public void testBadRequestLine() throws IOException {
        final String packet = "GET /";
        final HttpInputStream in = this.getStream(packet);
        assertThrows(HttpException.class, () -> this.parser.parse(in));
    }

    @Test
    public void testUnknownMethod() throws IOException {
        final String packet = "FROBNICATE / HTTP/1.1\r\n\r\n";
        final HttpInputStream in = this.getStream(packet);
        assertThrows(HttpException.class, () -> this.parser.parse(in));
    }

    @Test
    public void testBadVersion() throws IOException {
        final String packet = "GET / HTTPbis\r\n\r\n";
        final HttpInputStream in = this.getStream(packet);
        assertThrows(HttpException.class, () -> this.parser.parse(in));
    }

    @Test
    public void testFutureVersion() throws IOException {
        final String packet = "GET / HTTP/99.99";
        final HttpInputStream in = this.getStream(packet);
        assertThrows(HttpException.class, () -> this.parser.parse(in));
    }

    private HttpInputStream getStream(final String packet) {
        final byte[] bytes = packet.getBytes(StandardCharsets.ISO_8859_1);
        final ByteArrayInputStream source = new ByteArrayInputStream(bytes);
        return new HttpInputStream(source);
    }
}
