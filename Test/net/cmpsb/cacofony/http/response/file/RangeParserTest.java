package net.cmpsb.cacofony.http.response.file;

import net.cmpsb.cacofony.http.exception.BadRequestException;
import net.cmpsb.cacofony.http.exception.HttpException;
import net.cmpsb.cacofony.http.request.HeaderValueParser;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the range parser.
 *
 * @author Luc Everse
 */
public class RangeParserTest {
    private RangeParser parser;

    @Before
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

        assertThat("There are three ranges.",
                   ranges.size(),
                   is(3));

        assertThat("The ranges are correct.",
                   ranges,
                   contains(new Range(900, 1199), new Range(500, 700), new Range(1000, 1199)));
    }

    @Test(expected = HttpException.class)
    public void testParseInvertedRange() {
        final List<String> values = Arrays.asList(
            "percents=0-12", "88-",
            "bytes=500-12"
        );

        this.parser.parse(values, 1200);
    }

    @Test(expected = HttpException.class)
    public void testParseNegativeEnd() {
        final List<String> values = Arrays.asList(
                "percents=0-12", "88-",
                "bytes=500--900"
        );

        this.parser.parse(values, 1200);
    }

    @Test(expected = HttpException.class)
    public void testParseOverflowStart() {
        final List<String> values = Arrays.asList(
                "percents=0-12", "88-",
                "bytes=500-900"
        );

        this.parser.parse(values, 200);
    }

    @Test(expected = BadRequestException.class)
    public void testManyRanges() {
        final List<String> values = Arrays.asList(
                "a", "b", "c", "d",
                "e", "f", "g", "h",
                "i", "j", "k", "l",
                "m", "n", "o", "p",
                "q", "r", "s", "t"
        );

        this.parser.parse(values, 1200);
    }
}
