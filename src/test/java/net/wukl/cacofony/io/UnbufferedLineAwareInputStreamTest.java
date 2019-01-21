package net.wukl.cacofony.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the non-aware to line-aware input stream wrapper.
 *
 * @author Luc Everse
 */
public class UnbufferedLineAwareInputStreamTest
        extends LineAwareInputStreamTest<UnbufferedLineAwareInputStream> {

    public UnbufferedLineAwareInputStream getStream(final byte[] packet) {
        final ByteArrayInputStream source = new ByteArrayInputStream(packet);
        return new UnbufferedLineAwareInputStream(source);
    }

    @Test
    public void testReadAfterClosing() throws IOException {
        final byte[] packet = {
            'T', 'e', 's', 't', '\r', '\n'
        };
        final UnbufferedLineAwareInputStream in = this.getStream(packet);
        in.close();
        assertThrows(IOException.class, () -> in.read());
    }

    @Test
    public void testMarkNeverFails() throws IOException {
        final byte[] packet = {
                'T', 'e', 's', 't', '\r', '\n'
        };
        final UnbufferedLineAwareInputStream in = this.getStream(packet);
        in.close();
        in.mark(20);
    }

    @Test
    public void testMarkUnsupportedOnClosedStream() throws IOException {
        final byte[] packet = {
                'T', 'e', 's', 't', '\r', '\n'
        };
        final UnbufferedLineAwareInputStream in = this.getStream(packet);
        in.close();

        assertThat(in.markSupported()).isFalse();
    }

    @Test
    public void testMarkSupportedOnSupportingStream() throws IOException {
        final UnbufferedLineAwareInputStream in = new UnbufferedLineAwareInputStream(
                new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }

            @Override
            public boolean markSupported() {
                return true;
            }
        });

        assertThat(in.markSupported()).isTrue();
    }

    @Test
    public void testMarkSupportedOnNonSupportingStream() throws IOException {
        final UnbufferedLineAwareInputStream in = new UnbufferedLineAwareInputStream(
                new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return 0;
                    }

                    @Override
                    public boolean markSupported() {
                        return false;
                    }
                });

        assertThat(in.markSupported()).isFalse();
    }
}
