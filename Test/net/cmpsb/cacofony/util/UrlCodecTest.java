package net.cmpsb.cacofony.util;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author Luc Everse
 */
public class UrlCodecTest {
    private UrlCodec codec;

    @Before
    public void before() {
        this.codec = new UrlCodec();
    }

    @Test
    public void testEncodeAllowedSpecialCookieComponent() {
        final String allowedSpecialChars = "!#$&'*+-.^_`|~";
        final String encoded = this.codec.encodeCookieComponent(allowedSpecialChars);
        assertThat("The output is unchanged.",
                   encoded,
                   is(equalTo(allowedSpecialChars)));
    }

    @Test
    public void testEncodeSpace() {
        final String input = "String full of spaces.";
        final String output = this.codec.encodeCookieComponent(input);

        assertThat("The output is as expected.",
                   output,
                   is(equalTo("String%20full%20of%20spaces.")));
    }

    @Test
    public void testEncodeUtf8() {
        final String input = "møøse";
        final String output = this.codec.encodeCookieComponent(input);

        assertThat("The output is as expected.",
                   output,
                   is(equalTo("m%C3%B8%C3%B8se")));
    }
}
