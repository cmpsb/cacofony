package net.cmpsb.cacofony.http.request;

import net.cmpsb.cacofony.http.exception.BadRequestException;
import net.cmpsb.cacofony.io.HttpInputStream;
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

/**
 * Tests for the HTTP header parser.
 *
 * @author Luc Everse
 */
public class HeaderParserTest {
    private HeaderParser parser;

    @Before
    public void before() {
        this.parser = new HeaderParser();
    }

    @Test
    public void testValidSimple() throws IOException {
        final String req =
            "Host: cacofony.cmpsb.net\r\n"
          + "Accept: text/plain\r\n"
          + "\r\n";

        final HttpInputStream in = this.getStream(req);

        final MutableRequest request = new MutableRequest();
        this.parser.parse(in, request);

        assertThat("The Host header is parsed correctly.",
                   request.getHeaders("Host"),
                   is(equalTo(Collections.singletonList("cacofony.cmpsb.net"))));

        assertThat("The Accept header is parsed correctly.",
                   request.getHeaders("Accept"),
                   is(equalTo(Collections.singletonList("text/plain"))));
    }

    @Test
    public void testValidMulti() throws IOException {
        final String req =
            "Host: cacofony.cmpsb.net\r\n"
          + "Accept: text/plain\r\n"
          + "Accept: text/html\r\n"
          + "Content-Encoding: utf-8\r\n"
          + "\r\n";

        final HttpInputStream in = this.getStream(req);

        final MutableRequest request = new MutableRequest();
        this.parser.parse(in, request);

        assertThat("The Host header is parsed correctly.",
                   request.getHeaders("Host"),
                   is(equalTo(Collections.singletonList("cacofony.cmpsb.net"))));

        assertThat("The Accept headers are combined into a list.",
                   request.getHeaders("Accept"),
                   is(equalTo(Arrays.asList("text/plain", "text/html"))));

        assertThat("The Content-Encoding header is parsed correctly.",
                   request.getHeaders("Content-Encoding"),
                   is(equalTo(Collections.singletonList("utf-8"))));
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidSyntax() throws IOException {
        final String req = "Host : cacofony.cmpsb.net\r\n\r\n";

        final HttpInputStream in = this.getStream(req);
        final MutableRequest request = new MutableRequest();

        this.parser.parse(in, request);
    }

    @Test(expected = BadRequestException.class)
    public void testObsFold() throws IOException {
        final String req =
            "Host: cacofony.cmpsb.net\r\n"
          + "Accept: text/plain\r\n"
          + " text/html\r\n"
          + "\r\n";

        final HttpInputStream in = this.getStream(req);
        final MutableRequest request = new MutableRequest();

        this.parser.parse(in, request);
    }

    private HttpInputStream getStream(final byte[] bytes) {
        final ByteArrayInputStream source = new ByteArrayInputStream(bytes);

        return new HttpInputStream(source);
    }

    private HttpInputStream getStream(final String request) {
        final byte[] packet = request.getBytes(StandardCharsets.ISO_8859_1);

        return this.getStream(packet);
    }
}
