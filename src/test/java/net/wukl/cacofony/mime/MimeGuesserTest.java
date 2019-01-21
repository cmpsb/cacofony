package net.wukl.cacofony.mime;

import com.j256.simplemagic.ContentInfoUtil;
import net.wukl.cacofony.server.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the MIME guesser.
 *
 * @author Luc Everse
 */
public class MimeGuesserTest {
    private MimeGuesser guesser;

    @BeforeEach
    public void before() {
        final MimeParser mimeParser = new StrictMimeParser();
        final MimeDb mimeDb = new MimeDb();

        mimeDb.register("txt", MimeType.text());
        mimeDb.register("html", MimeType.html());
        mimeDb.register("bin", MimeType.octetStream());
        mimeDb.register("json", new MimeType("application", "json"));

        this.guesser = new MimeGuesser(mimeParser, mimeDb, new ContentInfoUtil());
    }

    @Test
    public void testExistingFile() throws IOException {
        final Path path = Files.createTempFile("cacafony_unit_test_", ".tmp");

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("<!DOCTYPE html><html></html>");
        }

        final MimeType type = this.guesser.guessLocal(path);

        assertThat(type).isEqualTo(MimeType.html());

        Files.delete(path);
    }

    @Test
    public void testLazyExtension() throws IOException {
        final Path path = Paths.get("J:\\test.txt");
        final MimeType type = this.guesser.guessLocal(path);

        assertThat(type).isEqualTo(MimeType.text());
    }

    @Test
    public void testUnknownFile() throws IOException {
        final Path path = Files.createTempFile("cacofony_unit_test_", ".ttttttttttt");

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("$IC#%(O*OIH#CPOBU #GK#H\"T<SH>");
        }

        final MimeType type = this.guesser.guessLocal(path);

        assertThat(type).isEqualTo(MimeType.octetStream());

        Files.delete(path);
    }

    @Test
    public void testLocalResource() throws IOException {
        final String location = "/net/wukl/cacofony/test/json.json";
        final MimeType type = this.guesser.guessLocal(Server.class, location);

        assertThat(type).isEqualTo(new MimeType("application", "json"));
    }

    @Test
    public void testLocalResourceWithoutExtension() throws IOException {
        final String location = "/net/wukl/cacofony/test/i";
        final MimeType type = this.guesser.guessLocal(Server.class, location);

        assertThat(type).isEqualTo(new MimeType("image", "x-ms-bmp"));
    }

    @Test
    public void testLocalResourceUnknown() throws IOException {
        final String location = "/net/wukl/cacofony/test/nmn";
        final MimeType type = this.guesser.guessLocal(Server.class, location);

        assertThat(type).isEqualTo(MimeType.octetStream());
    }
}
