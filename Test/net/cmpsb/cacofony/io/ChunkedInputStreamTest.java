package net.cmpsb.cacofony.io;

import net.cmpsb.cacofony.http.exception.BadRequestException;
import net.cmpsb.cacofony.http.request.HeaderParser;
import net.cmpsb.cacofony.http.request.MutableRequest;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the HTTP chunk input stream.
 *
 * @author Luc Everse
 */
public class ChunkedInputStreamTest {

    private MutableRequest request;
    private HeaderParser headerParser;

    private final byte[] singleChunkPacket = {
        'A', ';', 't', 'e', 's', 't', '=', 'y', 'e', 's', ';', 's', 'i', 'n', 'g', 'l', 'e',
        '\r', '\n',
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        '\r', '\n',
        '0',
        '\r', '\n',
        'C', 'h', 'e', 'c', 'k', 's', 'u', 'm', ':', ' ', '1', '4', '6', '1', '6', '7', '4', '2',
        '\r', '\n',
        '\r', '\n'
    };

    private final byte[] multiChunkPacket = {
        '4', ';', 't', 'e', 's', 't', '=', 'y', 'e', 's', ';', 'm', 'u', 'l', 't', 'i',
        '\r', '\n',
        0, 1, 2, 3,
        '\r', '\n',

        '3',
        '\r', '\n',
        4, 5, 6,
        '\r', '\n',

        '3',
        '\r', '\n',
        7, 8, 9,
        '\r', '\n',

        '0',
        '\r', '\n',
        'C', 'h', 'e', 'c', 'k', 's', 'u', 'm', ':', ' ', '1', '4', '6', '1', '6', '7', '4', '2',
        '\r', '\n',
        '\r', '\n'
    };

    private final byte[] truncatedChunk = {
        'F',
        '\r', '\n',
        0, 1
    };

    private final byte[] negativeChunk = {
        '-', 'A', 'A', 'A', '\r', '\n'
    };

    private final byte[] nonumericChunk = {
        'W', 'h', 'a', 't'
    };

    @Before
    public void before() {
        this.request = new MutableRequest();
        this.headerParser = new HeaderParser();
    }

    @Test
    public void testSingleReadSingleChunk() throws IOException {
        final ChunkedInputStream in = this.getStream(this.singleChunkPacket);

        for (int i = 0; i < 10; ++i) {
            final int value = in.read();
            if (value != i) {
                fail("Mismatch at read " + i + ": expected " + i + ", got " + value + ".");
            }
        }

        assertThat("The final read returns EOF.",
                   in.read(),
                   is(-1));

        this.validateTrailingHeaders();
    }

    @Test
    public void testSingleReadMultipleChunks() throws IOException {
        final ChunkedInputStream in = this.getStream(this.multiChunkPacket);

        for (int i = 0; i < 10; ++i) {
            final int value = in.read();
            if (value != i) {
                fail("Mismatch at read " + i + ": expected " + i + ", got " + value + ".");
            }
        }

        assertThat("The final read returns EOF.",
                in.read(),
                is(-1));

        this.validateTrailingHeaders();
    }

    @Test
    public void testBulkReadsSingleChunk() throws IOException {
        final ChunkedInputStream in = this.getStream(this.singleChunkPacket);

        final byte[] data = new byte[4];
        final int length = in.read(data);

        assertThat("4 bytes have been read.",
                   length,
                   is(4));

        assertThat("The read data is the correct part of the chunk body.",
                   data,
                   is(equalTo(new byte[] {0, 1, 2, 3})));
    }

    @Test
    public void testBulkReadsMultipleChunks() throws IOException {
        final ChunkedInputStream in = this.getStream(this.multiChunkPacket);

        final byte[] data = new byte[9];
        final int length = in.read(data);

        assertThat("9 bytes have been read.",
                   length,
                   is(9));

        assertThat("The read data is correctly taken from multiple successive chunks.",
                   data,
                   is(equalTo(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8})));
    }

    @Test
    public void testMoreThanAvailable() throws IOException {
        final ChunkedInputStream in = this.getStream(this.multiChunkPacket);

        final byte[] data = new byte[1024];
        final byte[] target = new byte[data.length];

        // Zero out both arrays.
        for (int i = 0; i < data.length; ++i) {
            data[i] = 0;
            target[i] = 0;
        }

        // Prepare the target we're expecting.
        for (byte i = 0; i < 10; ++i) {
            target[i] = i;
        }

        // Read the data.
        final int length = in.read(data);

        assertThat("The returned length is correct.",
                   length,
                   is(10));

        assertThat("Only the given chunks are read and the data is correct.",
                   data,
                   is(equalTo(target)));

        this.validateTrailingHeaders();

        final int zero = in.read(data);

        assertThat("Reading more returns 0 bytes.",
                   zero,
                   is(0));
    }

    @Test
    public void testTruncatedChunk() throws IOException {
        final ChunkedInputStream in = this.getStream(this.truncatedChunk);

        final byte[] data = new byte[10];

        for (int i = 0; i < data.length; ++i) {
            data[i] = 0;
        }

        final int length = in.read(data);

        assertThat("The returned amount is the amount it could read before EOF.",
                   length,
                   is(2));

        assertThat("The correct data is returned.",
                   data,
                   is(equalTo(new byte[] {0, 1, 0, 0, 0, 0, 0, 0, 0, 0})));
    }

    @Test(expected = BadRequestException.class)
    public void testNegativeChunkSize() throws IOException {
        final ChunkedInputStream in = this.getStream(this.negativeChunk);

        in.read();
    }

    @Test(expected = BadRequestException.class)
    public void testNonumericChunkSize() throws IOException {
        final ChunkedInputStream in = this.getStream(this.nonumericChunk);

        in.read();
    }

    @Test(expected = NullPointerException.class)
    public void testReadInvalidBuffer() throws IOException {
        final ChunkedInputStream in = this.getStream(this.singleChunkPacket);

        in.read(null, 0, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadInvalidOffset() throws IOException {
        final ChunkedInputStream in = this.getStream(this.singleChunkPacket);

        final byte[] buffer = new byte[20];
        in.read(buffer, -20, 20);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadNegativeLength() throws IOException {
        final ChunkedInputStream in = this.getStream(this.singleChunkPacket);

        final byte[] buffer = new byte[20];
        in.read(buffer, 0, -20);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadLongLength() throws IOException {
        final ChunkedInputStream in = this.getStream(this.singleChunkPacket);

        final byte[] buffer = new byte[20];
        in.read(buffer, 0, 2020);
    }

    private ChunkedInputStream getStream(final byte[] packet) throws IOException {
        final ByteArrayInputStream source = new ByteArrayInputStream(packet);
        final HttpInputStream in = new HttpInputStream(source);
        return new ChunkedInputStream(in, this.request, this.headerParser);
    }

    private void validateTrailingHeaders() {
        assertThat("The request object contains the Checksum header.",
                this.request.getHeader("Checksum"),
                is(equalTo("14616742")));
    }
}
