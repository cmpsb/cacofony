package net.cmpsb.cacofony.http.response.file;

import net.cmpsb.cacofony.http.exception.BadRequestException;
import net.cmpsb.cacofony.http.exception.HttpException;
import net.cmpsb.cacofony.http.request.HeaderValueParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the range parser.
 *
 * @author Luc Everse
 */
public class RangeParserTest {
    private RangeParser parser;

    @BeforeEach
    public void before() {
        this.parser = new RangeParser(new HeaderValueParser());
    }

    @Test
    public void testParseManyBytes() {
        final List<String> values = Arrays.asList(
            "percents=0-12", "88-",
            "bytes=-300", "500-700", "1000-"
        );

        final List<Range> ranges = this.parser.parse(values, 1200);

        assertThat(ranges)
                .hasSize(3)
                .contains(new Range(900, 1199), new Range(500, 700), new Range(1000, 1199));
    }

    @Test
    public void testParseInvertedRange() {
        final List<String> values = Arrays.asList(
            "percents=0-12", "88-",
            "bytes=500-12"
        );

        assertThrows(HttpException.class, () -> this.parser.parse(values, 1200));
    }

    @Test
    public void testParseNegativeEnd() {
        final List<String> values = Arrays.asList(
                "percents=0-12", "88-",
                "bytes=500--900"
        );

        assertThrows(HttpException.class, () -> this.parser.parse(values, 1200));
    }

    @Test
    public void testParseOverflowStart() {
        final List<String> values = Arrays.asList(
                "percents=0-12", "88-",
                "bytes=500-900"
        );

        assertThrows(HttpException.class, () -> this.parser.parse(values, 200));
    }

    @Test
    public void testManyRanges() {
        final List<String> values = Arrays.asList(
                "a", "b", "c", "d",
                "e", "f", "g", "h",
                "i", "j", "k", "l",
                "m", "n", "o", "p",
                "q", "r", "s", "t"
        );

        assertThrows(BadRequestException.class, () -> this.parser.parse(values, 1200));
    }
}
