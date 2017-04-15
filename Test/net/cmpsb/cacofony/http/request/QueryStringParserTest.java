package net.cmpsb.cacofony.http.request;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the query string parser.
 *
 * @author Luc Everse
 */
public class QueryStringParserTest {
    private QueryStringParser parser;

    @Before
    public void before() {
        this.parser = new QueryStringParser();
    }

    @Test
    public void testEmpty() {
        final String queryString = "?";
        final Map<String, String> values = this.parser.parse(queryString);

        assertThat("The map is empty.",
                   values.isEmpty(),
                   is(true));
    }

    @Test
    public void testSimpleSingle() {
        final String queryString = "?id=2233";
        final Map<String, String> values = this.parser.parse(queryString);

        assertThat("The map contains an entry <id, 2233>.",
                   values.get("id"),
                   is(equalTo("2233")));
    }

    @Test
    public void testSimpleMulti() {
        final String queryString = "?page=about&lang=en_us";
        final Map<String, String> values = this.parser.parse(queryString);

        assertThat("The map contains an entry <page, about>.",
                   values.get("page"),
                   is(equalTo("about")));

        assertThat("The map contains an entry <lang, en_us>.",
                   values.get("lang"),
                   is(equalTo("en_us")));
    }

    @Test
    public void testUnvaluedMulti() {
        final String queryString = "?form=contact&noborder&name=John";
        final Map<String, String> values = this.parser.parse(queryString);

        assertThat("The map contains the entry <form, contact>",
                   values.get("form"),
                   is(equalTo("contact")));

        assertThat("The map contains the key noborder.",
                   values.containsKey("noborder"),
                   is(true));

        assertThat("The map contains the entry <name, John>.",
                   values.get("name"),
                   is(equalTo("John")));
    }

    @Test
    public void testEscapedMulti() {
        final String queryString =
            "?url=http%3A%2F%2Fcmpsb.net%2Fsubmit%3Fns%3D2%26mail%3Dtest%40cmpsb.net"
          + "&date=2015-02-02";


        final Map<String, String> values = this.parser.parse(queryString);

        assertThat("The map contains an entry <url, ...>",
                   values.get("url"),
                   is(equalTo("http://cmpsb.net/submit?ns=2&mail=test@cmpsb.net")));

        assertThat("The map contains an entry <date, 2015-02-02>.",
                   values.get("date"),
                   is(equalTo("2015-02-02")));
    }
}
