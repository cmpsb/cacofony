package net.cmpsb.cacofony.http.response.file;

import net.cmpsb.cacofony.http.cookie.CookieWriter;
import net.cmpsb.cacofony.http.request.HeaderValueParser;
import net.cmpsb.cacofony.http.response.ResponsePreparer;
import net.cmpsb.cacofony.http.response.ResponseWriter;
import net.cmpsb.cacofony.mime.MimeType;
import net.cmpsb.cacofony.server.MutableServerSettings;
import net.cmpsb.cacofony.server.ServerProperties;
import net.cmpsb.cacofony.server.ServerSettings;
import net.cmpsb.cacofony.util.UrlCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for file responses.
 *
 * @author Luc Everse
 */
public class FileResponseTest {
    private ResponsePreparer preparer;
    private ResponseWriter writer;

    @BeforeEach
    public void before() {
        final ServerSettings settings = new MutableServerSettings();
        final ServerProperties properties = new ServerProperties();
        final UrlCodec urlCodec = new UrlCodec();
        final CookieWriter cookieWriter = new CookieWriter(urlCodec);
        this.preparer = new ResponsePreparer(settings, properties, cookieWriter);

        this.writer = new ResponseWriter(settings, new HeaderValueParser());
    }

    @Test
    public void testConstructor() throws IOException {
        final File tmp = this.temp();
        final FileResponse response = new FileResponse(tmp);

        assertThat(response.getLastModified()).as("last modified").isEqualTo(tmp.lastModified());
    }

    @Test
    public void testEtagChangesWithModificationDate() throws IOException, InterruptedException {
        final File olderFile = this.temp();
        final File newerFile = this.temp();
        assert newerFile.setLastModified(olderFile.lastModified() + 1000);

        final FileResponse olderResponse = new FileResponse(olderFile);
        final FileResponse newerResponse = new FileResponse(newerFile);

        assertThat(olderResponse).isNotEqualTo(newerResponse);
    }

    @Test
    public void testUnrangedRequest() throws IOException {
        final String content = "Test string for testing!";
        final File tmp = this.temp(content);
        final FileResponse response = new FileResponse(tmp);
        response.setContentType(MimeType.text());
        response.setBufferSize(8);

        this.preparer.prepare(null, response);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final OutputStream outc = this.writer.write(null, response, out);
        outc.close();

        final String sResponse = out.toString("UTF-8");

        assertThat(response.getContentType()).as("content type").isEqualTo(MimeType.text());
        assertThat(response.getHeaders()).as("headers").doesNotContainKey("Content-Range");
        assertThat(sResponse).contains(content);
    }

    @Test
    public void testSingleRangeRequest() throws IOException {
        final File tmp = this.temp("Test string for testing!");
        final FileResponse response = new FileResponse(tmp);
        response.setContentType(MimeType.text());

        final Range range = new Range(5, 10);
        response.setRanges(Collections.singletonList(range));

        this.preparer.prepare(null, response);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final OutputStream outc = this.writer.write(null, response, out);
        outc.close();

        final String sResponse = out.toString("UTF-8");

        assertThat(response.getContentType()).as("content type").isEqualTo(MimeType.text());
        assertThat(response.getHeaders().get("Content-Range")).as("Content-Range header")
                .hasSize(1)
                .element(0).asString().isEqualTo("bytes 5-10/24");
        assertThat(sResponse)
                .contains("string")
                .doesNotContain(" for testing!");
    }

    @Test
    public void testMultiRangeRequest() throws IOException {
        final File tmp = this.temp("Test string for testing!");
        final FileResponse response = new FileResponse(tmp);
        response.setContentType(MimeType.text());

        final Range rangeOne = new Range(5, 10);
        final Range rangeTwo = new Range(16, 23);
        response.setRanges(Arrays.asList(rangeOne, rangeTwo));

        this.preparer.prepare(null, response);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final OutputStream outc = this.writer.write(null, response, out);
        outc.close();

        final String sResponse = out.toString("UTF-8");

        assertThat(response.getContentType()).as("content type").isEqualTo(MimeType.byteranges());
        assertThat(sResponse).containsSubsequence(
                "Content-Range: bytes 5-10/24",
                "string",
                "Content-Range: bytes 16-23/24",
                "testing!"
        ).doesNotContain("Test ");
    }

    @Test
    public void testWriteInvalidRange() throws IOException {
        final File tmp = this.temp("Test string for testing!");
        final FileResponse response = new FileResponse(tmp);
        response.setContentType(MimeType.text());

        final Range range = new Range(5, 1102);
        response.setRanges(Collections.singletonList(range));

        this.preparer.prepare(null, response);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertThrows(IOException.class, () -> this.writer.write(null, response, out));
    }

    @Test
    public void testWriteInvalidRanges() throws IOException {
        final File tmp = this.temp("Test string for testing!");
        final FileResponse response = new FileResponse(tmp);
        response.setContentType(MimeType.text());

        final Range rangeOne = new Range(5, 1122);
        final Range rangeTwo = new Range(16, 23);
        response.setRanges(Arrays.asList(rangeOne, rangeTwo));

        this.preparer.prepare(null, response);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertThrows(IOException.class, () -> this.writer.write(null, response, out));
    }

    @Test
    public void testWriteDeletedFile() throws IOException {
        final File tmp = this.temp("Test string for testing!");
        final FileResponse response = new FileResponse(tmp);
        response.setContentType(MimeType.text());

        this.preparer.prepare(null, response);

        tmp.delete();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertThrows(IOException.class, () -> this.writer.write(null, response, out));
    }

    @Test
    public void testWriteDeletedWithRange() throws IOException {
        final File tmp = this.temp("Test string for testing!");
        final FileResponse response = new FileResponse(tmp);
        response.setContentType(MimeType.text());

        final Range range = new Range(5, 10);
        response.setRanges(Collections.singletonList(range));

        this.preparer.prepare(null, response);

        tmp.delete();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertThrows(IOException.class, () -> this.writer.write(null, response, out));
    }

    @Test
    public void testWriteDeletedWithRanges() throws IOException {
        final File tmp = this.temp("Test string for testing!");
        final FileResponse response = new FileResponse(tmp);
        response.setContentType(MimeType.text());

        final Range rangeOne = new Range(5, 10);
        final Range rangeTwo = new Range(16, 23);
        response.setRanges(Arrays.asList(rangeOne, rangeTwo));

        this.preparer.prepare(null, response);

        tmp.delete();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertThrows(IOException.class, () -> this.writer.write(null, response, out));
    }

    @Test
    public void testSetInvalidRange() throws IOException {
        final File tmp = this.temp();
        final FileResponse response = new FileResponse(tmp);

        assertThrows(IllegalArgumentException.class, () -> response.setBufferSize(-200));
    }

    private File temp() throws IOException {
        final File file = File.createTempFile("cacofony_unit_test_file_", ".tmp");

        file.deleteOnExit();

        return file;
    }

    private File temp(final String content) throws IOException {
        final File file = this.temp();

        try (FileWriter fw = new FileWriter(file)) {
            fw.write(content);
        }

        return file;
    }
}
