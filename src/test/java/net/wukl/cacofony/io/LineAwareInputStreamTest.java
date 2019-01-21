package net.wukl.cacofony.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(first).isEqualTo("First line");
        assertThat(second).isEqualTo("Second");
        assertThat(empty).isEmpty();
    }

    @Test
    public void testOddCodePoints() throws IOException {
        final byte[] packet = {
                'T', 'e', '\n', 's', 't', '\r', 's', 't', 'r', 'i', 'n', 'g', '\r', '\n'
        };

        final T in = this.getStream(packet);

        final String testString = in.readLine();
        final String empty = in.readLine();

        assertThat(testString).isEqualTo("Te\nst\rstring");
        assertThat(empty).isEmpty();
    }

    @Test
    public void testDoubleCr() throws IOException {
        final byte[] packet = {
                'T', 'e', 's', 't', '\r', '\r', '\n'
        };

        final T in = this.getStream(packet);

        final String test = in.readLine();
        final String empty = in.readLine();

        assertThat(test).isEqualTo("Test\r");
        assertThat(empty).isEmpty();
    }

    @Test
    public void testUnterminatedString() throws IOException {
        final byte[] packet = {
                'T', 'e', 's', 't'
        };

        final T in = this.getStream(packet);

        final String test = in.readLine();
        final String empty = in.readLine();

        assertThat(test).isEqualTo("Test");
        assertThat(empty).isEmpty();
    }

    @Test
    public void testUnterminatedStringWithTrailingCr() throws IOException {
        final byte[] packet = {
                'C', 'R', '\r'
        };

        final T in = this.getStream(packet);

        final String cr = in.readLine();
        final String empty = in.readLine();

        assertThat(cr).isEqualTo("CR\r");
        assertThat(empty).isEmpty();
    }
}
