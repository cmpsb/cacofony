package net.cmpsb.cacofony.http.request;

import net.cmpsb.cacofony.http.exception.BadRequestException;
import net.cmpsb.cacofony.io.HttpInputStream;
import net.cmpsb.cacofony.util.Ob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the HTTP header parser.
 *
 * @author Luc Everse
 */
public class HeaderParserTest {
    private HeaderParser parser;

    @BeforeEach
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

        final Map<String, List<String>> headers = this.parser.parse(in);

        assertThat(headers).isEqualTo(Ob.map(
                "host", Collections.singletonList("cacofony.cmpsb.net"),
                "accept", Collections.singletonList("text/plain")
        ));
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

        final Map<String, List<String>> headers = this.parser.parse(in);

        assertThat(headers).isEqualTo(Ob.map(
                "host", Collections.singletonList("cacofony.cmpsb.net"),
                "accept", Arrays.asList("text/plain", "text/html"),
                "content-encoding", Collections.singletonList("utf-8")
        ));
    }

    @Test
    public void testInvalidSyntax() throws IOException {
        final String req = "Host : cacofony.cmpsb.net\r\n\r\n";

        final HttpInputStream in = this.getStream(req);

        assertThrows(BadRequestException.class, () -> this.parser.parse(in));
    }

    @Test
    public void testObsFold() throws IOException {
        final String req =
            "Host: cacofony.cmpsb.net\r\n"
          + "Accept: text/plain\r\n"
          + " text/html\r\n"
          + "\r\n";

        final HttpInputStream in = this.getStream(req);

        assertThrows(BadRequestException.class, () -> this.parser.parse(in));
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
