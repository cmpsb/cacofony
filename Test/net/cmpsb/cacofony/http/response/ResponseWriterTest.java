package net.cmpsb.cacofony.http.response;

import net.cmpsb.cacofony.http.cookie.CookieWriter;
import net.cmpsb.cacofony.http.request.HeaderValueParser;
import net.cmpsb.cacofony.http.request.Method;
import net.cmpsb.cacofony.http.request.MutableRequest;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.server.MutableServerSettings;
import net.cmpsb.cacofony.server.ServerProperties;
import net.cmpsb.cacofony.util.UrlCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Luc Everse
 */
public class ResponseWriterTest {
    private MutableServerSettings settings;

    private HeaderValueParser valueParser;
    private ResponseWriter writer;

    private ResponsePreparer preparer;

    @BeforeEach
    public void before() {
        this.settings = new MutableServerSettings();

        this.valueParser = new HeaderValueParser();
        this.writer = new ResponseWriter(this.settings, this.valueParser);

        final ServerProperties properties = new ServerProperties();
        final UrlCodec urlCodec = new UrlCodec();
        final CookieWriter cookieWriter = new CookieWriter(urlCodec);
        this.preparer = new ResponsePreparer(this.settings, properties, cookieWriter);
    }

    @Test
    public void testWritePlainResponse() throws IOException {
        final String content = "Test string for testing!";

        final Request request = new MutableRequest(Method.GET, "/", 1, 1);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Response response = new TextResponse(content);

        this.preparer.prepare(request, response);

        final OutputStream outc = this.writer.write(request, response, out);
        outc.close();

        final String serialResponse = out.toString("UTF-8");

        assertThat(serialResponse)
                .startsWith("HTTP/1.1 200 OK")
                .contains("Server", "Content-Length: " + content.length(), "Date")
                .contains("Content-Type: text/plain", content);
    }

    @Test
    public void testWritePlainResponseToHeadRequest() throws IOException {
        final String content = "Test string for testing!";

        final Request request = new MutableRequest(Method.HEAD, "/", 1, 1);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Response response = new TextResponse(content);

        this.preparer.prepare(request, response);

        final OutputStream outc = this.writer.write(request, response, out);
        outc.close();

        final String serialResponse = out.toString("UTF-8");

        assertThat(serialResponse)
                .startsWith("HTTP/1.1 200 OK")
                .contains("Server", "Content-Length: " + content.length(), "Date")
                .contains("Content-Type: text/plain")
                .doesNotContain(content);
    }

    @Test
    public void testWriteStraightEncodedResponse() throws IOException {
        final String plainContent = "Hello, Cacofony!";
        final byte[] gzipContent = {
            0x1f, (byte) 0x8b, 0x08, 0x00, 0x3c,
            0x35, (byte) 0xd5, 0x58, 0x02, 0x03,
            (byte) 0xf3, 0x48, (byte) 0xcd, (byte) 0xc9, (byte) 0xc9,
            (byte) 0xd7, 0x51, 0x70, 0x4e, 0x4c,
            (byte) 0xce, 0x4f, (byte) 0xcb, (byte) 0xcf, (byte) 0xab,
            0x54, (byte) 0xe4, 0x02, 0x00, 0x59,
            (byte) 0xfe, 0x35, 0x16, 0x11, 0x00,
            0x00
        };

        final MutableRequest request = new MutableRequest(Method.GET, "/", 1, 0);
        request.getHeaders().put("accept-encoding", Collections.singletonList("gzip"));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Response response = new TextResponse(plainContent);
        response.setCompressionAllowed(true);

        this.preparer.prepare(request, response);

        final OutputStream outc = this.writer.write(request, response, out);
        outc.close();

        final String serialResponse = out.toString("UTF-8");

        assertThat(serialResponse)
                .startsWith("HTTP/1.0 200 OK")
                .contains("Content-Length: " + gzipContent.length, "Content-Encoding: gzip")
                .contains("Content-Type: text/plain")
                .doesNotContain(plainContent);
    }

    @Test
    public void testWriteStraightEncodedResponsetoHeadRequest() throws IOException {
        final String plainContent = "Hello, Cacofony!";
        final byte[] gzipContent = {
                0x1f, (byte) 0x8b, 0x08, 0x00, 0x3c,
                0x35, (byte) 0xd5, 0x58, 0x02, 0x03,
                (byte) 0xf3, 0x48, (byte) 0xcd, (byte) 0xc9, (byte) 0xc9,
                (byte) 0xd7, 0x51, 0x70, 0x4e, 0x4c,
                (byte) 0xce, 0x4f, (byte) 0xcb, (byte) 0xcf, (byte) 0xab,
                0x54, (byte) 0xe4, 0x02, 0x00, 0x59,
                (byte) 0xfe, 0x35, 0x16, 0x11, 0x00,
                0x00
        };

        final MutableRequest request = new MutableRequest(Method.HEAD, "/", 1, 0);
        request.getHeaders().put("accept-encoding", Collections.singletonList("gzip"));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Response response = new TextResponse(plainContent);
        response.setCompressionAllowed(true);

        this.preparer.prepare(request, response);

        final OutputStream outc = this.writer.write(request, response, out);
        outc.close();

        final String serialResponse = out.toString("UTF-8");
        final String serialGzipContent = new String(gzipContent, StandardCharsets.ISO_8859_1);

        assertThat(serialResponse)
                .startsWith("HTTP/1.0 200 OK")
                .contains("Content-Length: " + gzipContent.length, "Content-Encoding: gzip")
                .contains("Content-Type: text/plain")
                .doesNotContain(plainContent)
                .doesNotContain(serialGzipContent);
    }

    @Test
    public void testWriteStraightEncodedResponseWithAcceptMismatch() throws IOException {
        final String plainContent = "Hello, Cacofony!";

        final MutableRequest request = new MutableRequest(Method.GET, "/", 1, 0);
        request.getHeaders().put("accept-encoding", Collections.singletonList("no existing enc"));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Response response = new TextResponse(plainContent);
        response.setCompressionAllowed(true);

        this.preparer.prepare(request, response);

        final OutputStream outc = this.writer.write(request, response, out);
        outc.close();

        final String serialResponse = out.toString("UTF-8");

        assertThat(serialResponse).doesNotContain("Content-Encoding").contains(plainContent);
    }

    @Test
    public void testWriteStraightEncodedResponseWithoutServerSupport() throws IOException {
        this.settings.setCompressionEnabled(false);

        final String plainContent = "Hello, Cacofony!";

        final MutableRequest request = new MutableRequest(Method.GET, "/", 1, 0);
        request.getHeaders().put("accept-encoding", Collections.singletonList("gzip"));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Response response = new TextResponse(plainContent);
        response.setCompressionAllowed(true);

        this.preparer.prepare(request, response);

        final OutputStream outc = this.writer.write(request, response, out);
        outc.close();

        final String serialResponse = out.toString("UTF-8");

        assertThat(serialResponse).doesNotContain("Content-Encoding").contains(plainContent);
    }

    @Test
    public void testWriteStreamedResponse() throws IOException {
        final String content = "Test string for testing!";
        final byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);

        final Request request = new MutableRequest(Method.GET, "/", 1, 0);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Response response = new StreamedResponse(stream -> stream.write(contentBytes));

        this.preparer.prepare(request, response);

        final OutputStream outc = this.writer.write(request, response, out);
        outc.close();

        final String serialResponse = out.toString("UTF-8");

        assertThat(serialResponse)
                .startsWith("HTTP/1.0 200 OK")
                .doesNotContain("Content-Length")
                .contains("Transfer-Encoding: chunked", content);
    }

    @Test
    public void testWriteStreamedResponseToHeadRequest() throws IOException {
        final String content = "Test string for testing!";
        final byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);

        final Request request = new MutableRequest(Method.HEAD, "/", 1, 0);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Response response = new StreamedResponse(stream -> stream.write(contentBytes));

        this.preparer.prepare(request, response);

        final OutputStream outc = this.writer.write(request, response, out);
        outc.close();

        final String serialResponse = out.toString("UTF-8");

        assertThat(serialResponse)
                .startsWith("HTTP/1.0 200 OK")
                .doesNotContain("Content-Length")
                .contains("Transfer-Encoding: chunked")
                .doesNotContain(content);
    }

    @Test
    public void testWriteErrorResponse() throws IOException {
        final String content = "bad request content!";

        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final Response response = new TextResponse(ResponseCode.BAD_REQUEST, content);

        this.preparer.prepare(null, response);

        final OutputStream outc = this.writer.write(null, response, out);
        outc.close();

        final String serialResponse = out.toString("UTF-8");

        assertThat(serialResponse)
            .startsWith("HTTP/1.0 400 Bad Request")
            .contains(content);
    }
}
