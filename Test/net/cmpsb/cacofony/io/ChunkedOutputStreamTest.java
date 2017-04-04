package net.cmpsb.cacofony.io;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the chunked output stream.
 *
 * @author Luc Everse
 */
public class ChunkedOutputStreamTest {
    private ByteArrayOutputStream target;

    @Before
    public void before() {
        this.target = new ByteArrayOutputStream();
    }

    @Test
    public void testCloseImmediately() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target);
        out.close();

        assertThat("The buffer contains only the 0-length chunk.",
                   this.target.toByteArray(),
                   is(equalTo(new byte[] {'0', '\r', '\n', '\r', '\n'})));

    }

    @Test
    public void testSuccessiveSingleWrites() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target, 5);

        for (int i = 1; i <= 12; ++i) {
            out.write(i);
        }

        out.close();

        final byte[] expected = {
            '5', '\r', '\n',
            1, 2, 3, 4, 5,
            '\r', '\n',

            '5', '\r', '\n',
            6, 7, 8, 9, 10,
            '\r', '\n',

            '2', '\r', '\n',
            11, 12,
            '\r', '\n',

            '0', '\r', '\n',
            '\r', '\n'
        };

        assertThat("The output buffer is as expected.",
                   this.target.toByteArray(),
                   is(equalTo(expected)));
    }

    @Test
    public void testSmallWrite() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target);

        out.write(new byte[] {1, 2, 3});
        out.close();

        final byte[] expected = {
            '3', '\r', '\n',
            1, 2, 3,
            '\r', '\n',

            '0', '\r', '\n',
            '\r', '\n'
        };

        assertThat("The output buffer is as expected.",
                   this.target.toByteArray(),
                   is(equalTo(expected)));
    }

    @Test
    public void testWriteAcrossChunks() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target, 5);
        out.write(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        out.close();

        final byte[] expected = {
            '5', '\r', '\n',
            1, 2, 3, 4, 5,
            '\r', '\n',

            '5', '\r', '\n',
            6, 7, 8, 9, 10,
            '\r', '\n',

            '2', '\r', '\n',
            11, 12,
            '\r', '\n',

            '0', '\r', '\n',
            '\r', '\n'
        };

        assertThat("The output buffer is as expected.",
                   this.target.toByteArray(),
                   is(equalTo(expected)));
    }

    @Test(expected = NullPointerException.class)
    public void testInvalidWriteBuffer() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target);

        out.write(null, 0, 10);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInvalidOffset() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target);

        out.write(new byte[10], -2, 10);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInvalidLength() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target);

        out.write(new byte[10], 0, -10);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testOobWrite() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target);

        out.write(new byte[10], 0, 100000);
    }

    @Test(expected = IOException.class)
    public void testSingleWriteToClosedStream() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target);
        out.close();
        out.write(2);
    }

    @Test(expected = IOException.class)
    public void testBulkWriteToClosedStream() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target);
        out.close();
        out.write(new byte[] {20, 20, 20, 20});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithBadSize() {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target, -1020);
    }
}
