package net.cmpsb.cacofony.mime;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the mime parser.
 *
 * @author Luc Everse
 */
public abstract class MimeParserTest<P extends MimeParser> {
    private P parser;

    public abstract P getParser();

    @Before
    public void before() {
        this.parser = this.getParser();
    }

    @Test
    public void testSimple() {
        final String raw = "text/plain";
        final MimeType type = this.parser.parse(raw);

        assertThat("The type is correct.",
                   type.getMainType(),
                   is(equalTo("text")));

        assertThat("The subtype is correct.",
                   type.getSubType(),
                   is(equalTo("plain")));
    }

    @Test
    public void testAnySubtype() {
        final String raw = "image/*";
        final MimeType type = this.parser.parse(raw);

        assertThat("The type is correct.",
                   type.getMainType(),
                   is(equalTo("image")));

        assertThat("The subtype is correct.",
                   type.getSubType(),
                   is(equalTo("*")));
    }

    @Test
    public void testAny() {
        final String raw = "*/*";
        final MimeType type = this.parser.parse(raw);

        assertThat("The type is correct.",
                   type.getMainType(),
                   is(equalTo("*")));

        assertThat("The subtype is correct.",
                   type.getSubType(),
                   is(equalTo("*")));
    }

    @Test
    public void testSimpleWithParameters() {
        final String raw = "text/html+xml; encoding=utf-8; q=0.9";
        final MimeType type = this.parser.parse(raw);

        assertThat("The type is correct.",
                   type.getMainType(),
                   is(equalTo("text")));

        assertThat("The subtype is correct.",
                   type.getSubType(),
                   is(equalTo("html+xml")));

        assertThat("The first parameter is present and correct.",
                   type.getParameters().get("encoding"),
                   is(equalTo("utf-8")));

        assertThat("The second parameter is present and correct.",
                   type.getParameters().get("q"),
                   is(equalTo("0.9")));
    }
}
