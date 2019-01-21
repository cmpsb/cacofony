package net.wukl.cacofony.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Luc Everse
 */
public class UrlCodecTest {
    private UrlCodec codec;

    @BeforeEach
    public void before() {
        this.codec = new UrlCodec();
    }

    @Test
    public void testEncodeAllowedSpecialCookieComponent() {
        final String allowedSpecialChars = "!#$&'*+-.^_`|~";
        final String encoded = this.codec.encodeCookieComponent(allowedSpecialChars);

        assertThat(encoded).isEqualTo(allowedSpecialChars);
    }

    @Test
    public void testEncodeSpace() {
        final String input = "String full of spaces.";
        final String output = this.codec.encodeCookieComponent(input);

        assertThat(output).isEqualTo("String%20full%20of%20spaces.");
    }

    @Test
    public void testEncodeUtf8() {
        final String input = "møøse";
        final String output = this.codec.encodeCookieComponent(input);

        assertThat(output).isEqualTo("m%C3%B8%C3%B8se");
    }
}
