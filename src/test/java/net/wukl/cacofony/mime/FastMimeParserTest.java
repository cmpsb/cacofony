package net.wukl.cacofony.mime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Luc Everse
 */
public class FastMimeParserTest extends MimeParserTest<FastMimeParser> {
    @Override
    public FastMimeParser getParser() {
        return new FastMimeParser();
    }

    @Test
    public void testJavaAccept() {
        final String raw = "*; q=.2";
        final MimeType type = this.parser.parse(raw);

        assertThat(type.getMainType()).as("main type").isEqualTo("*");
        assertThat(type.getSubType()).as("sub type").isEqualTo("*");
        assertThat(type.getParameters()).as("parameters").containsEntry("q", ".2").hasSize(1);
    }

}
