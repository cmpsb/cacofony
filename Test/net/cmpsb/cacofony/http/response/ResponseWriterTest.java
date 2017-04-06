package net.cmpsb.cacofony.http.response;

import net.cmpsb.cacofony.http.request.HeaderValueParser;
import net.cmpsb.cacofony.http.request.Method;
import net.cmpsb.cacofony.http.request.MutableRequest;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.server.MutableServerSettings;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

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

        this.preparer = new ResponsePreparer(this.settings);
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
