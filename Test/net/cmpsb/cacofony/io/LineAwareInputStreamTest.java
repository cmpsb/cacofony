package net.cmpsb.cacofony.io;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;

/**
 * Generic tests for line-aware input streams.
 *
 * @author Luc Everse
 */
public abstract class LineAwareInputStreamTest<T extends LineAwareInputStream> {
    public abstract T getStream(byte[] bytes);

    public T getStream(final String packet) {
        final byte[] bytes = packet.getBytes(StandardCharsets.ISO_8859_1);
        return this.getStream(bytes);
    }

    @Test
    public void testReadLine() throws IOException {
        final byte[] packet = {
                'F', 'i', 'r', 's', 't', ' ', 'l', 'i', 'n', 'e', '\r', '\n',
                'S', 'e', 'c', 'o', 'n', 'd',  '\r', '\n'
        };

        final T in = this.getStream(packet);

        final String first = in.readLine();
        final String second = in.readLine();
        final String empty = in.readLine();

        assertThat("The first string has been read correctly.",
                first,
                is(equalTo("First line")));

        assertThat("The second string has been read correctly.",
                second,
                is(equalTo("Second")));

        assertThat("The final string is empty.",
                empty,
                isEmptyString());
    }

    @Test
    public void testOddCodePoints() throws IOException {
        final byte[] packet = {
                'T', 'e', '\n', 's', 't', '\r', 's', 't', 'r', 'i', 'n', 'g', '\r', '\n'
        };

        final T in = this.getStream(packet);

        final String testString = in.readLine();
        final String empty = in.readLine();

        assertThat("The first string is copied correctly.",
                testString,
                is(equalTo("Te\nst\rstring")));

        assertThat("The final string is empty.",
                empty,
                isEmptyString());
    }

    @Test
    public void testDoubleCr() throws IOException {
        final byte[] packet = {
                'T', 'e', 's', 't', '\r', '\r', '\n'
        };

        final T in = this.getStream(packet);

        final String test = in.readLine();
        final String empty = in.readLine();

        assertThat("The string includes the extra CR.",
                test,
                is(equalTo("Test\r")));

        assertThat("The final string is empty.",
                empty,
                isEmptyString());
    }

    @Test
    public void testUnterminatedString() throws IOException {
        final byte[] packet = {
                'T', 'e', 's', 't'
        };

        final T in = this.getStream(packet);

        final String test = in.readLine();
        final String empty = in.readLine();

        assertThat("The string is copied until the end of the buffer.",
                test,
                is(equalTo("Test")));

        assertThat("The final string is empty.",
                empty,
                isEmptyString());
    }

    @Test
    public void testUnterminatedStringWithTrailingCr() throws IOException {
        final byte[] packet = {
                'C', 'R', '\r'
        };

        final T in = this.getStream(packet);

        final String cr = in.readLine();
        final String empty = in.readLine();

        assertThat("The string is copied with the trailing CR.",
                cr,
                is(equalTo("CR\r")));

        assertThat("The final string is empty.",
                empty,
                isEmptyString());
    }
}
