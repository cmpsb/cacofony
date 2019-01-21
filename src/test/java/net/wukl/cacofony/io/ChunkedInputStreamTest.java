package net.wukl.cacofony.io;

import net.wukl.cacofony.http.exception.BadRequestException;
import net.wukl.cacofony.http.exception.SilentException;
import net.wukl.cacofony.http.request.HeaderParser;
import net.wukl.cacofony.http.request.MutableRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

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

    private final byte[] truncatedMultiChunk = {
        '2',
        '\r', '\n',
        0, 1,
        '\r', '\n',

        '4',
        '\r', '\n',
        0, 1
    };

    private final byte[] negativeChunk = {
        '-', 'A', 'A', 'A', '\r', '\n'
    };

    private final byte[] nonumericChunk = {
        'W', 'h', 'a', 't'
    };

    @BeforeEach
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

        assertThat(in.read()).as("next byte").isEqualTo(-1);

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

        assertThat(in.read()).as("next byte").isEqualTo(-1);

        this.validateTrailingHeaders();
    }

    @Test
    public void testBulkReadsSingleChunk() throws IOException {
        final ChunkedInputStream in = this.getStream(this.singleChunkPacket);

        final byte[] data = new byte[4];
        final int length = in.read(data);

        assertThat(length).as("reported number of bytes").isEqualTo(4);
        assertThat(data).containsExactly(0, 1, 2, 3);
    }

    @Test
    public void testBulkReadsMultipleChunks() throws IOException {
        final ChunkedInputStream in = this.getStream(this.multiChunkPacket);

        final byte[] data = new byte[9];
        final int length = in.read(data);

        assertThat(length).as("reported number of bytes").isEqualTo(9);
        assertThat(data).containsExactly(0, 1, 2, 3, 4, 5, 6, 7, 8);
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

        assertThat(length).as("reported number of bytes").isEqualTo(10);
        assertThat(data).isEqualTo(target);

        this.validateTrailingHeaders();

        final int eof = in.read(data);
        assertThat(eof).as("last byte").isEqualTo(-1);
    }

    @Test
    public void testTruncatedChunk() throws IOException {
        final ChunkedInputStream in = this.getStream(this.truncatedChunk);

        final byte[] data = new byte[10];
        assertThrows(SilentException.class, () -> in.read(data));
    }

    @Test
    public void testTruncatedMultiChunk() throws IOException {
        final ChunkedInputStream in = this.getStream(this.truncatedMultiChunk);

        final byte[] data = new byte[10];
        assertThrows(SilentException.class, () -> in.read(data));
    }

    @Test
    public void testSkipSingleChunk() throws IOException {
        final ChunkedInputStream in = this.getStream(this.singleChunkPacket);

        final long skipped = in.skip(4);
        assertThat(skipped).as("number of bytes reported skipped").isEqualTo(4);

        final byte[] data = new byte[4];
        final int length = in.read(data);

        assertThat(length).as("reported number of bytes").isEqualTo(data.length);
        assertThat(data).containsExactly(4, 5, 6, 7);
    }

    @Test
    public void testSkipMultiChunk() throws IOException {
        final ChunkedInputStream in = this.getStream(this.multiChunkPacket);

        final long skipped = in.skip(6);
        assertThat(skipped).as("number of bytes reported skipped").isEqualTo(6);

        final byte[] data = new byte[4];
        final int length = in.read(data);

        assertThat(length).as("reported number of bytes").isEqualTo(data.length);
        assertThat(data).containsExactly(6, 7, 8, 9);
    }

    @Test
    public void testSkipTruncatedPacket() throws IOException {
        final ChunkedInputStream in = this.getStream(this.truncatedChunk);

        assertThrows(SilentException.class, () -> in.skip(4));
    }

    @Test
    public void testSkipBeyondPacket() throws IOException {
        final ChunkedInputStream in = this.getStream(this.multiChunkPacket);

        final long skipped = in.skip(Long.MAX_VALUE);
        assertThat(skipped).as("number of bytes reported skipped").isGreaterThanOrEqualTo(10);

        final long nextSkipped = in.skip(Long.MAX_VALUE);
        assertThat(nextSkipped).isEqualTo(0);

        final int nextRead = in.read();
        assertThat(nextRead).as("last byte").isEqualTo(-1);
    }

    @Test
    public void testSkipNegativeBytes() throws IOException {
        final ChunkedInputStream in = this.getStream(this.singleChunkPacket);

        assertThat(in.skip(-10)).isEqualTo(0);
    }

    @Test
    public void testNegativeChunkSize() throws IOException {
        final ChunkedInputStream in = this.getStream(this.negativeChunk);

        assertThrows(BadRequestException.class, () -> in.read());
    }

    @Test
    public void testNonumericChunkSize() throws IOException {
        final ChunkedInputStream in = this.getStream(this.nonumericChunk);

        assertThrows(BadRequestException.class, () -> in.read());
    }

    @Test
    public void testReadInvalidBuffer() throws IOException {
        final ChunkedInputStream in = this.getStream(this.singleChunkPacket);

        assertThrows(NullPointerException.class, () -> in.read(null, 0, 0));
    }

    @Test
    public void testReadInvalidOffset() throws IOException {
        final ChunkedInputStream in = this.getStream(this.singleChunkPacket);

        final byte[] buffer = new byte[20];
        assertThrows(IndexOutOfBoundsException.class, () -> in.read(buffer, -20, 20));
    }

    @Test
    public void testReadNegativeLength() throws IOException {
        final ChunkedInputStream in = this.getStream(this.singleChunkPacket);

        final byte[] buffer = new byte[20];
        assertThrows(IndexOutOfBoundsException.class, () -> in.read(buffer, 0, -20));
    }

    @Test
    public void testReadLongLength() throws IOException {
        final ChunkedInputStream in = this.getStream(this.singleChunkPacket);

        final byte[] buffer = new byte[20];
        assertThrows(IndexOutOfBoundsException.class, () -> in.read(buffer, 0, 2020));
    }

    @Test
    public void testReadFromClosedStream() throws IOException {
        final ChunkedInputStream in = this.getStream(this.singleChunkPacket);
        in.close();

        assertThrows(IOException.class, () -> in.read());
    }

    private ChunkedInputStream getStream(final byte[] packet) throws IOException {
        final ByteArrayInputStream source = new ByteArrayInputStream(packet);
        final HttpInputStream in = new HttpInputStream(source);
        return new ChunkedInputStream(in, this.request, this.headerParser);
    }

    private void validateTrailingHeaders() {
        assertThat(this.request.getHeaders()).as("headers")
                .containsEntry("checksum", Collections.singletonList("14616742"));
    }
}
