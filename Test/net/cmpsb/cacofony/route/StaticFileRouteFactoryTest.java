package net.cmpsb.cacofony.route;

import net.cmpsb.cacofony.http.exception.NotFoundException;
import net.cmpsb.cacofony.http.request.HeaderValueParser;
import net.cmpsb.cacofony.http.request.Method;
import net.cmpsb.cacofony.http.request.MutableRequest;
import net.cmpsb.cacofony.http.response.ResponseCode;
import net.cmpsb.cacofony.http.response.file.FileResponse;
import net.cmpsb.cacofony.http.response.file.RangeParser;
import net.cmpsb.cacofony.mime.MimeGuesser;
import org.junit.Before;
import org.junit.Test;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

/**
 * @author Luc Everse
 */
public class StaticFileRouteFactoryTest {
    private StaticFileRouteFactory factory;

    @Before
    public void before() {
        final PathCompiler pathCompiler = mock(PathCompiler.class);
        final HeaderValueParser headerValueParser = new HeaderValueParser();
        final RangeParser rangeParser = mock(RangeParser.class);
        final MimeGuesser mimeGuesser = mock(MimeGuesser.class);

        this.factory = new StaticFileRouteFactory(
            pathCompiler, headerValueParser, rangeParser, mimeGuesser
        );
    }

    @Test
    public void testRequestExistingFile() throws Exception {
        final String content = "temporary file for testing!";

        final Path tempDir = Files.createTempDirectory("cacofony_temp_dir_");
        final Path temp = Files.createTempFile(tempDir, "cacofony_temp_file_", ".tmp");

        try (OutputStream out = Files.newOutputStream(temp)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }

        final String path = temp.getFileName().toString();

        final RoutingEntry entry = this.factory.build("/dir", tempDir);
        final MutableRequest request = new MutableRequest(Method.GET, "/dir/" + path, 1, 1);
        request.setPathParameters(Collections.singletonMap("file", path));

        final FileResponse response = (FileResponse) entry.getAction().handle(request);

        assertThat("The response is not null.",
                   response,
                   is(notNullValue()));

        assertThat("The response code is OK.",
                   response.getStatus(),
                   is(equalTo(ResponseCode.OK)));

        assertThat("The content length is equal to the file's.",
                   response.getContentLength(),
                   is(equalTo((long) content.length())));

        Files.delete(temp);
        Files.delete(tempDir);
    }

    @Test
    public void testRequestNotModified() throws Exception {
        final String content = "temporary file for testing!";

        final Path tempDir = Files.createTempDirectory("cacofony_temp_dir_");
        final Path temp = Files.createTempFile(tempDir, "cacofony_temp_file_", ".tmp");

        try (OutputStream out = Files.newOutputStream(temp)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }

        final String path = temp.getFileName().toString();

        final FileResponse res = new FileResponse(temp.toFile());
        final String etag = res.getEtag();

        final RoutingEntry entry = this.factory.build("/dir", tempDir);
        final MutableRequest request = new MutableRequest(Method.GET, "/dir/" + path, 1, 1);
        request.setPathParameters(Collections.singletonMap("file", path));
        request.getHeaders().put("If-None-Match", Collections.singletonList(etag));

        final FileResponse response = (FileResponse) entry.getAction().handle(request);

        assertThat("The response is not null.",
                   response,
                   is(notNullValue()));

        assertThat("The status code is Not Modified.",
                   response.getStatus(),
                   is(equalTo(ResponseCode.NOT_MODIFIED)));

        assertThat("The content length is equal to the file's.",
                   response.getContentLength(),
                   is(equalTo((long) content.length())));

        Files.delete(temp);
        Files.delete(tempDir);
    }

    @Test(expected = NotFoundException.class)
    public void testRequestNonexistentFile() throws Exception {
        final String path = "thaoethoeaoeahteoathoethoaohtoeaht.hmtl";
        final RoutingEntry entry = this.factory.build("/dir", this.cwd());
        final MutableRequest request = new MutableRequest(Method.GET, "/dir/" + path, 1, 1);
        request.setPathParameters(Collections.singletonMap("file", path));

        entry.getAction().handle(request);
    }

    @Test(expected = NotFoundException.class)
    public void testRequestParentPath() throws Exception {
        final String path = "../file.txt";
        final RoutingEntry entry = this.factory.build("/dir", this.cwd());
        final MutableRequest request = new MutableRequest(Method.GET, "/dir/" + path, 1, 1);
        request.setPathParameters(Collections.singletonMap("file", path));

        entry.getAction().handle(request);
    }

    private Path cwd() {
        return Paths.get("").toAbsolutePath();
    }
}
