package net.cmpsb.cacofony.http.request;

import net.cmpsb.cacofony.http.exception.HttpException;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;

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
}
