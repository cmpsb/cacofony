package net.cmpsb.cacofony.mime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the mime parser.
 *
 * @author Luc Everse
 */
public abstract class MimeParserTest<P extends MimeParser> {
    public P parser;

    public abstract P getParser();

    @BeforeEach
    public void before() {
        this.parser = this.getParser();
    }

    @Test
    public void testSimple() {
        final String raw = "text/plain";
        final MimeType type = this.parser.parse(raw);

        assertThat(type.getMainType()).as("main type").isEqualTo("text");
        assertThat(type.getSubType()).as("sub type").isEqualTo("plain");
        assertThat(type.getParameters()).as("parameters").isEmpty();
    }

    @Test
    public void testAnySubtype() {
        final String raw = "image/*";
        final MimeType type = this.parser.parse(raw);

        assertThat(type.getMainType()).as("main type").isEqualTo("image");
        assertThat(type.getSubType()).as("sub type").isEqualTo("*");
        assertThat(type.getParameters()).as("parameters").isEmpty();
    }

    @Test
    public void testAny() {
        final String raw = "*/*";
        final MimeType type = this.parser.parse(raw);

        assertThat(type.getMainType()).as("main type").isEqualTo("*");
        assertThat(type.getSubType()).as("sub type").isEqualTo("*");
        assertThat(type.getParameters()).as("parameters").isEmpty();
    }

    @Test
    public void testSimpleWithParameters() {
        final String raw = "text/html+xml; encoding=utf-8; q=0.9";
        final MimeType type = this.parser.parse(raw);

        assertThat(type.getMainType()).as("main type").isEqualTo("text");
        assertThat(type.getSubType()).as("sub type").isEqualTo("html+xml");
        assertThat(type.getParameters()).as("parameters")
                .containsEntry("encoding", "utf-8")
                .containsEntry("q", "0.9");
    }
}
