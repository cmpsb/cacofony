package net.cmpsb.cacofony.http.response;

import net.cmpsb.cacofony.http.cookie.CookieWriter;
import net.cmpsb.cacofony.http.request.HeaderValueParser;
import net.cmpsb.cacofony.http.request.Method;
import net.cmpsb.cacofony.http.request.MutableRequest;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.server.MutableServerSettings;
import net.cmpsb.cacofony.server.ServerProperties;
import net.cmpsb.cacofony.util.UrlCodec;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * @author Luc Everse
 */
public class ResponseWriterTest {
    private MutableServerSettings settings;

    private HeaderValueParser valueParser;
    private ResponseWriter writer;

    private ResponsePreparer preparer;

    @Before
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

        assertThat("The response contains the correct start line.",
                   serialResponse,
                   containsString("HTTP/1.1 200 OK"));

        assertThat("The response contains the Server header.",
                   serialResponse,
                   containsString("Server"));

        assertThat("The response contains the correct content length.",
                   serialResponse,
                   containsString("Content-Length: " + content.length()));

        assertThat("The response contains a Date header.",
                   serialResponse,
                   containsString("Date"));

        assertThat("The response has the text/plain default encoding.",
                   serialResponse,
                   containsString("Content-Type: text/plain"));

        assertThat("The response contains the content.",
                   serialResponse,
                   containsString(content));
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

        assertThat("The response contains the correct start line.",
                serialResponse,
                containsString("HTTP/1.1 200 OK"));

        assertThat("The response contains the Server header.",
                serialResponse,
                containsString("Server"));

        assertThat("The response contains the correct content length.",
                serialResponse,
                containsString("Content-Length: " + content.length()));

        assertThat("The response contains a Date header.",
                serialResponse,
                containsString("Date"));

        assertThat("The response has the text/plain default encoding.",
                serialResponse,
                containsString("Content-Type: text/plain"));

        assertThat("The response does not contain the content.",
                serialResponse,
                not(containsString(content)));
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

        assertThat("The response contains the correct start line.",
                serialResponse,
                containsString("HTTP/1.0 200 OK"));

        assertThat("The content length is that of the compressed data.",
                serialResponse,
                containsString("Content-Length: " + gzipContent.length));

        assertThat("The encoding is indicated.",
                   serialResponse,
                   containsString("Content-Encoding: gzip"));

        assertThat("The response does not contain the content.",
                    serialResponse,
                    not(containsString(plainContent)));
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

        assertThat("The response contains the correct start line.",
                serialResponse,
                containsString("HTTP/1.0 200 OK"));

        assertThat("The content length is that of the compressed data.",
                serialResponse,
                containsString("Content-Length: " + gzipContent.length));

        assertThat("The encoding is indicated.",
                serialResponse,
                containsString("Content-Encoding: gzip"));

        assertThat("The response does not contain the content.",
                serialResponse,
                not(containsString(plainContent)));
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

        assertThat("The response is not encoded.",
                   serialResponse,
                   not(containsString("Content-Encoding")));

        assertThat("The content is sent as plain text.",
                   serialResponse,
                   containsString(plainContent));
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

        assertThat("The response is not encoded.",
                serialResponse,
                not(containsString("Content-Encoding")));

        assertThat("The content is sent as plain text.",
                serialResponse,
                containsString(plainContent));
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

        assertThat("The response contains the correct start line.",
                   serialResponse,
                   containsString("HTTP/1.0 200 OK"));

        assertThat("The response contains no content length.",
                   serialResponse,
                   not(containsString("Content-Length")));

        assertThat("The response indicates it's chunked.",
                   serialResponse,
                   containsString("Transfer-Encoding: chunked"));

        assertThat("The response contains the content.",
                   serialResponse,
                   containsString(content));
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

        assertThat("The response contains the correct start line.",
                serialResponse,
                containsString("HTTP/1.0 200 OK"));

        assertThat("The response contains no content length.",
                serialResponse,
                not(containsString("Content-Length")));

        assertThat("The response indicates it's chunked.",
                serialResponse,
                containsString("Transfer-Encoding: chunked"));

        assertThat("The response does not contain the content.",
                serialResponse,
                not(containsString(content)));
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

        assertThat("The response is in HTTP/1.0.",
                   serialResponse,
                   containsString("HTTP/1.0"));

        assertThat("There is some content.",
                   serialResponse,
                   containsString(content));
    }
}
