package net.cmpsb.cacofony.http.response.file;

import net.cmpsb.cacofony.http.request.HeaderValueParser;
import net.cmpsb.cacofony.http.response.ResponsePreparer;
import net.cmpsb.cacofony.http.response.ResponseWriter;
import net.cmpsb.cacofony.mime.MimeType;
import net.cmpsb.cacofony.server.MutableServerSettings;
import net.cmpsb.cacofony.server.ServerSettings;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Tests for file responses.
 *
 * @author Luc Everse
 */
public class FileResponseTest {
    private ResponsePreparer preparer;
    private ResponseWriter writer;

    @Before
    public void before() {
        final ServerSettings settings = new MutableServerSettings();
        this.preparer = new ResponsePreparer(settings);

        this.writer = new ResponseWriter(settings, new HeaderValueParser());
    }

    @Test
    public void testConstructor() throws IOException {
        final File tmp = this.temp();
        final FileResponse response = new FileResponse(tmp);

        assertThat("The modification date is equal to that of the input file.",
                   response.getLastModified(),
                   is(equalTo(tmp.lastModified())));
    }

    @Test
    public void testEtagChangesWithModificationDate() throws IOException, InterruptedException {
        final File olderFile = this.temp();
        final File newerFile = this.temp();
        assert newerFile.setLastModified(olderFile.lastModified() + 1000);

        final FileResponse olderResponse = new FileResponse(olderFile);
        final FileResponse newerResponse = new FileResponse(newerFile);

        assertThat("The ETag changes when the modification date changes.",
                   olderResponse,
                   is(not(equalTo(newerResponse))));
    }

    @Test
    public void testUnrangedRequest() throws IOException {
        final File tmp = this.temp("Test string for testing!");
        final FileResponse response = new FileResponse(tmp);
        response.setContentType(MimeType.text());
        response.setBufferSize(8);

        this.preparer.prepare(null, response);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final OutputStream outc = this.writer.write(null, response, out);
        outc.close();

        final String sResponse = out.toString("UTF-8");

        assertThat("The content type is the original type.",
                   response.getContentType(),
                   is(equalTo(MimeType.text())));

        assertThat("There is no Content-Range header.",
                   response.getHeaders().containsKey("Content-Range"),
                   is(false));

        assertThat("The response contains the full string.",
                   sResponse,
                   containsString("Test string for testing!"));
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

        assertThat("The content type is the original type.",
                response.getContentType(),
                is(equalTo(MimeType.text())));

        assertThat("A Content-Range header is prevent.",
                response.getHeaders().get("Content-Range").get(0),
                is(equalTo("bytes 5-10/24")));

        assertThat("The response contains the range.",
                   sResponse,
                   containsString("string"));

        assertThat("The response doesn't contain the rest of the content.",
                   sResponse,
                   not(containsString(" for testing!")));
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

        assertThat("The content type is multipart/byteranges.",
                response.getContentType(),
                is(equalTo(new MimeType("multipart", "byteranges"))));

        assertThat("A Content-Range header for the first range is present.",
                   sResponse,
                   containsString("Content-Range: bytes 5-10/24"));

        assertThat("The first range is present.",
                   sResponse,
                   containsString("string"));

        assertThat("A Content-Range header for teh second range is present.",
                   sResponse,
                   containsString("Content-Range: bytes 16-23/24"));

        assertThat("The second range is present.",
                   sResponse,
                   containsString("testing!"));

        assertThat("The rest of the content is not present.",
                   sResponse,
                   not(containsString("Test ")));
    }

    @Test(expected = IOException.class)
    public void testWriteInvalidRange() throws IOException {
        final File tmp = this.temp("Test string for testing!");
        final FileResponse response = new FileResponse(tmp);
        response.setContentType(MimeType.text());

        final Range range = new Range(5, 1102);
        response.setRanges(Collections.singletonList(range));

        this.preparer.prepare(null, response);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final OutputStream outc = this.writer.write(null, response, out);
    }

    @Test(expected = IOException.class)
    public void testWriteInvalidRanges() throws IOException {
        final File tmp = this.temp("Test string for testing!");
        final FileResponse response = new FileResponse(tmp);
        response.setContentType(MimeType.text());

        final Range rangeOne = new Range(5, 1122);
        final Range rangeTwo = new Range(16, 23);
        response.setRanges(Arrays.asList(rangeOne, rangeTwo));

        this.preparer.prepare(null, response);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final OutputStream outc = this.writer.write(null, response, out);
    }

    @Test(expected = IOException.class)
    public void testWriteDeletedFile() throws IOException {
        final File tmp = this.temp("Test string for testing!");
        final FileResponse response = new FileResponse(tmp);
        response.setContentType(MimeType.text());

        this.preparer.prepare(null, response);

        tmp.delete();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final OutputStream outc = this.writer.write(null, response, out);
    }

    @Test(expected = IOException.class)
    public void testWriteDeletedWithRange() throws IOException {
        final File tmp = this.temp("Test string for testing!");
        final FileResponse response = new FileResponse(tmp);
        response.setContentType(MimeType.text());

        final Range range = new Range(5, 10);
        response.setRanges(Collections.singletonList(range));

        this.preparer.prepare(null, response);

        tmp.delete();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final OutputStream outc = this.writer.write(null, response, out);
    }

    @Test(expected = IOException.class)
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
        final OutputStream outc = this.writer.write(null, response, out);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetInvalidRange() throws IOException {
        final File tmp = this.temp();
        final FileResponse response = new FileResponse(tmp);

        response.setBufferSize(-200);
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
