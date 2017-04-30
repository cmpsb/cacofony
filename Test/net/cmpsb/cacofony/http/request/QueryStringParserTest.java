package net.cmpsb.cacofony.http.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the query string parser.
 *
 * @author Luc Everse
 */
public class QueryStringParserTest {
    private QueryStringParser parser;

    @BeforeEach
    public void before() {
        this.parser = new QueryStringParser();
    }

    @Test
    public void testEmpty() {
        final String queryString = "?";
        final Map<String, String> values = this.parser.parse(queryString);

        assertThat(values).isEmpty();
    }

    @Test
    public void testSimpleSingle() {
        final String queryString = "?id=2233";
        final Map<String, String> values = this.parser.parse(queryString);

        assertThat(values).hasSize(1);
        assertThat(values.get("id")).as("The id entry").isEqualTo("2233");
    }

    @Test
    public void testSimpleMulti() {
        final String queryString = "?page=about&lang=en_us";
        final Map<String, String> values = this.parser.parse(queryString);

        assertThat(values).hasSize(2);
        assertThat(values.get("page")).as("The page entry").isEqualTo("about");
        assertThat(values.get("lang")).as("The lang entry").isEqualTo("en_us");
    }

    @Test
    public void testUnvaluedMulti() {
        final String queryString = "?form=contact&noborder&name=John";
        final Map<String, String> values = this.parser.parse(queryString);

        assertThat(values).hasSize(3);
        assertThat(values.get("form")).as("The form entry").isEqualTo("contact");
        assertThat(values.containsKey("noborder")).as("The noborder entry is present").isTrue();
        assertThat(values.get("name")).as("The name entry").isEqualTo("John");
    }

    @Test
    public void testEscapedMulti() {
        final String queryString =
            "?url=http%3A%2F%2Fcmpsb.net%2Fsubmit%3Fns%3D2%26mail%3Dtest%40cmpsb.net"
          + "&date=2015-02-02";


        final Map<String, String> values = this.parser.parse(queryString);

        assertThat(values.get("url")).as("The url entry")
                .isEqualTo("http://cmpsb.net/submit?ns=2&mail=test@cmpsb.net");
        assertThat(values.get("date")).as("The date entry").isEqualTo("2015-02-02");
    }
}
