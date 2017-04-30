package net.cmpsb.cacofony.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the chunked output stream.
 *
 * @author Luc Everse
 */
public class ChunkedOutputStreamTest {
    private ByteArrayOutputStream target;

    @BeforeEach
    public void before() {
        this.target = new ByteArrayOutputStream();
    }

    @Test
    public void testCloseImmediately() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target);
        out.close();

        assertThat(this.target.toByteArray()).containsExactly('0', '\r', '\n', '\r', '\n');
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

        assertThat(this.target.toByteArray()).isEqualTo(expected);
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

        assertThat(this.target.toByteArray()).isEqualTo(expected);
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

        assertThat(this.target.toByteArray()).isEqualTo(expected);
    }

    @Test
    public void testWriteBulkIntoNearlyFull() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target, 16);
        out.write(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        out.write(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
                              1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        out.close();

        final byte[] expected = {
            '1', '0', '\r', '\n',
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3, 4,
            '\r', '\n',

            '1', '0', '\r', '\n',
            5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3, 4, 5, 6, 7, 8,
            '\r', '\n',

            '4', '\r', '\n',
            9, 10, 11, 12,
            '\r', '\n',

            '0', '\r', '\n',
            '\r', '\n'
        };

        assertThat(this.target.toByteArray()).isEqualTo(expected);
    }

    @Test
    public void testInvalidWriteBuffer() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target);

        assertThrows(NullPointerException.class, () -> out.write(null, 0, 10));
    }

    @Test
    public void testInvalidOffset() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target);

        assertThrows(IndexOutOfBoundsException.class, () -> out.write(new byte[10], -2, 10));
    }

    @Test
    public void testInvalidLength() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target);

        assertThrows(IndexOutOfBoundsException.class, () -> out.write(new byte[10], 0, -10));
    }

    @Test
    public void testOobWrite() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target);

        assertThrows(IndexOutOfBoundsException.class, () -> out.write(new byte[10], 0, 100000));
    }

    @Test
    public void testSingleWriteToClosedStream() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target);
        out.close();
        assertThrows(IOException.class, () -> out.write(2));
    }

    @Test
    public void testBulkWriteToClosedStream() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target);
        out.close();
        assertThrows(IOException.class, () -> out.write(new byte[] {20, 20, 20, 20}));
    }

    @Test
    public void testConstructWithBadSize() {
        assertThrows(IllegalArgumentException.class, () ->
                new ChunkedOutputStream(this.target, -1020)
        );
    }
}
