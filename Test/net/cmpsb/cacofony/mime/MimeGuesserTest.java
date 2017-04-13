package net.cmpsb.cacofony.mime;

import com.j256.simplemagic.ContentInfoUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the MIME guesser.
 *
 * @author Luc Everse
 */
public class MimeGuesserTest {
    private MimeGuesser guesser;

    @Before
    public void before() {
        final MimeParser mimeParser = new StrictMimeParser();
        final MimeDb mimeDb = new MimeDb();

        mimeDb.register("txt", MimeType.text());
        mimeDb.register("html", MimeType.html());
        mimeDb.register("bin", MimeType.octetStream());

        this.guesser = new MimeGuesser(mimeParser, mimeDb, new ContentInfoUtil());
    }

    @Test
    public void testExistingFile() throws IOException {
        final Path path = Files.createTempFile("cacafony_unit_test_", ".tmp");

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("<!DOCTYPE html><html></html>");
        }

        final MimeType type = this.guesser.guessLocal(path);

        assertThat("The file is correctly read text/html.",
                   type,
                   is(equalTo(MimeType.html())));

        Files.delete(path);
    }

    @Test
    public void testLazyExtension() throws IOException {
        final Path path = Paths.get("J:\\test.txt");
        final MimeType type = this.guesser.guessLocal(path);

        assertThat("The file was lazily guessed as text/plain.",
                   type,
                   is(equalTo(MimeType.text())));
    }

    @Test
    public void testUnknownFile() throws IOException {
        final Path path = Files.createTempFile("cacofony_unit_test_", ".ttttttttttt");

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("$IC#%(O*OIH#CPOBU #GK#H\"T<SH>");
        }

        final MimeType type = this.guesser.guessLocal(path);

        assertThat("The guesser gave up and returned application/octet-stream.",
                   type,
                   is(equalTo(MimeType.octetStream())));

        Files.delete(path);
    }
}
