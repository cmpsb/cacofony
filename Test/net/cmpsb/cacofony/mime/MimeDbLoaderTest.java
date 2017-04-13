package net.cmpsb.cacofony.mime;

import net.cmpsb.cacofony.util.Ob;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the standard MIME DB loader.
 *
 * @author Luc Everse
 */
public class MimeDbLoaderTest {
    private MimeDbLoader loader;
    private Map<String, MimeType> db;

    @Before
    public void before() {
        this.loader = new MimeDbLoader(new StrictMimeParser());
        this.db = new HashMap<>();
    }

    @Test
    public void testLoadSingle() {
        final InputStream file = this.stream("audio/sounds sounds");
        this.loader.load(file, this.db::put);

        assertThat("The DB contains the right association.",
                   this.db.get("sounds"),
                   is(equalTo(new MimeType("audio", "sounds"))));
    }

    @Test
    public void testLoadMultiExtension() {
        final InputStream file = this.stream("audio/visual eyes ears nose");
        this.loader.load(file, this.db::put);

        final MimeType expected = new MimeType("audio", "visual");

        assertThat("The DB contains the first extension.",
                   this.db.get("eyes"),
                   is(equalTo(expected)));

        assertThat("The DB contains the second extension.",
                   this.db.get("ears"),
                   is(equalTo(expected)));

        assertThat("The DB contains the third extension.",
                   this.db.get("nose"),
                   is(equalTo(expected)));
    }

    @Test
    public void testLoadComment() {
        final InputStream file = this.stream("# Test content for testing!");
        this.loader.load(file, this.db::put);

        assertThat("The DB is empty.",
                   this.db.isEmpty(),
                   is(true));
    }

    @Test
    public void testLoadExtensionLessType() {
        final InputStream file = this.stream("type/without-extension");
        this.loader.load(file, this.db::put);

        assertThat("The DB is empty.",
                this.db.isEmpty(),
                is(true));
    }

    @Test
    public void testMixed() {
        final InputStream file = this.stream(
            "# Header!\n"
          + "# Header, continued!\n"
          + "application/first-type first 1st\n"
          + "# comment/type aww\n"
          + "extension-less/type\n"
          + "application/second-type second"
        );

        this.loader.load(file, this.db::put);

        assertThat("The DB is not empty.",
                   this.db.isEmpty(),
                   is(false));

        assertThat("The first key is correct.",
                   this.db.get("first"),
                   is(equalTo(new MimeType("application", "first-type"))));

        assertThat("The second key is correct.",
                   this.db.get("1st"),
                   is(equalTo(new MimeType("application", "first-type"))));

        assertThat("The third key is correct.",
                   this.db.get("second"),
                   is(equalTo(new MimeType("application", "second-type"))));

        this.db.keySet().removeAll(Ob.set("first", "1st", "second"));

        assertThat("After removing all extensions that should have been present, the DB is empty.",
                   this.db.isEmpty(),
                   is(true));
    }

    private InputStream stream(final String contents) {
        final byte[] bytes = contents.getBytes(StandardCharsets.UTF_8);
        return new ByteArrayInputStream(bytes);
    }
}
