package net.wukl.cacofony.mime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for the standard MIME DB loader.
 *
 * @author Luc Everse
 */
public class MimeDbLoaderTest {
    private MimeDbLoader loader;
    private Map<String, MimeType> db;

    @BeforeEach
    public void before() {
        this.loader = new MimeDbLoader(new StrictMimeParser());
        this.db = new HashMap<>();
    }

    @Test
    public void testLoadSingle() {
        final InputStream file = this.stream("audio/sounds sounds");
        this.loader.load(file, this.db::put);

        Assertions.assertThat(this.db).containsEntry("sounds", new MimeType("audio", "sounds"));
    }

    @Test
    public void testLoadMultiExtension() {
        final InputStream file = this.stream("audio/visual eyes ears nose");
        this.loader.load(file, this.db::put);

        final MimeType expected = new MimeType("audio", "visual");

        Assertions.assertThat(this.db)
                .containsEntry("eyes", expected)
                .containsEntry("ears", expected)
                .containsEntry("nose", expected);
    }

    @Test
    public void testLoadComment() {
        final InputStream file = this.stream("# Test content for testing!");
        this.loader.load(file, this.db::put);

        Assertions.assertThat(this.db).isEmpty();
    }

    @Test
    public void testLoadExtensionLessType() {
        final InputStream file = this.stream("type/without-extension");
        this.loader.load(file, this.db::put);

        Assertions.assertThat(this.db).isEmpty();
    }

    @Test
    public void testMixed() {
        final InputStream file = this.stream(
            "# Header!\n"
          + "# Header, continued!\n"
          + "application/first-type first 1st\n"
          + "# comment/type aww\n"
          + "extension-less/type\n"
          + "text/second-type second"
        );

        this.loader.load(file, this.db::put);

        final MimeType firstType = new MimeType("application", "first-type");
        final MimeType secondType = new MimeType("text", "second-type");

        Assertions.assertThat(this.db)
                .hasSize(3)
                .containsEntry("first", firstType)
                .containsEntry("1st", firstType)
                .containsEntry("second", secondType);
    }

    private InputStream stream(final String contents) {
        final byte[] bytes = contents.getBytes(StandardCharsets.UTF_8);
        return new ByteArrayInputStream(bytes);
    }
}
