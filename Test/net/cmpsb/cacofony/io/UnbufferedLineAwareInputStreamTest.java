package net.cmpsb.cacofony.io;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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

    @Test(expected = IOException.class)
    public void testReadAfterClosing() throws IOException {
        final byte[] packet = {
            'T', 'e', 's', 't', '\r', '\n'
        };
        final UnbufferedLineAwareInputStream in = this.getStream(packet);
        in.close();
        in.read();
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

        assertThat("Marking is unsupported on a closed stream.",
                   in.markSupported(),
                   is(false));
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

        assertThat("Marking is supported if the source stream supports it.",
                   in.markSupported(),
                   is(true));
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

        assertThat("Marking is not supported if the source stream doesn't support it.",
                in.markSupported(),
                is(false));
    }
}
