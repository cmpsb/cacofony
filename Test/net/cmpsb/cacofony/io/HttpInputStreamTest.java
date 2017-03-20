package net.cmpsb.cacofony.io;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

/**
 * @author Luc Everse
 */
public class HttpInputStreamTest extends LineAwareInputStreamTest<HttpInputStream> {
    private byte[] dummyPacket =  {
            'd', 'a', 't', 'a'
    };

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithInvalidSize() {
        final ByteArrayInputStream source = new ByteArrayInputStream(new byte[0]);
        final HttpInputStream in = new HttpInputStream(source, -1024);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithInvalidSource() {
        new HttpInputStream(null);
    }

    @Test
    public void testRead() throws IOException {
        final byte[] packet = {
            14, 61, 67, 42
        };

        final HttpInputStream in = this.getStream(packet);

        final int value = in.read();

        assertThat("The read value is correct.",
                   value,
                   is(14));
    }

    @Test
    public void testReadLots() throws IOException {
        final Random random = new Random(4);
        final int[] bigData = random.ints().limit(262144).map(i -> i & 255).toArray();
        final byte[] data = new byte[bigData.length];

        for (int i = 0; i < bigData.length; ++i) {
            data[i] = (byte) bigData[i];
        }

        final HttpInputStream in = this.getStream(data);

        for (int i = 0; i < bigData.length; ++i) {
            final int value = in.read();
            if (value != bigData[i]) {
                fail("At index " + i + ": expected " + bigData[i] + ", got " + value + ".");
            }
        }

        assertThat("The last read returns EOF.",
                   in.read(),
                   is(-1));
    }

    @Test
    public void testReadSmallAmountIntoBuffer() throws IOException {
        final byte[] packet = {
            14, 61, 67, 42
        };

        final HttpInputStream in = this.getStream(packet);

        in.read();

        final byte[] buffer = new byte[3];
        final int length = in.read(buffer, 0, 3);

        assertThat("The read length is exactly 3.",
                   length,
                   is(3));

        assertThat("The contents match.",
                   buffer,
                   is(equalTo(new byte[] {61, 67, 42})));

        final int eof = in.read(buffer, 0, 3);

        assertThat("The next read returns EOF.",
                   eof,
                   is(-1));
    }

    @Test
    public void testReadLotsIntoBuffer() throws IOException {
        final Random random = new Random(4);
        final int[] bigData = random.ints().limit(262200).map(i -> i & 255).toArray();
        final byte[] data = new byte[bigData.length];

        for (int i = 0; i < bigData.length; ++i) {
            data[i] = (byte) bigData[i];
        }

        final HttpInputStream in = this.getStream(data);

        // Read a few bytes to shock the buffer.
        in.read();
        final int targetBinLength = 2;
        final byte[] bin = new byte[targetBinLength];
        final int binLength = in.read(bin, 0, targetBinLength);
        assert binLength == targetBinLength;

        final byte[] dataCopy = new byte[65600];
        final int length = in.read(dataCopy, 100, 65400);

        assertThat("The returned length is (for this context) equal to the requested length.",
                   length,
                   is(65400));

        for (int i = 100; i < 65400 + 100; ++i) {
            final byte value = dataCopy[i];
            if (value != data[i + 3 - 100]) {
                fail("At index " + i + ": expected " + data[i + 3 - 100] + ", got " + value + ".");
            }
        }

        // Do it again, but now for the next 65400 bytes.
        final int length2 = in.read(dataCopy, 100, 65400);

        assertThat("The returned length is (for this context) equal to the requested length.",
                length2,
                is(65400));

        for (int i = 100; i < 65400 + 100; ++i) {
            final byte value = dataCopy[i];
            if (value != data[i + 3 - 100 + 65400]) {
                fail("At index " + i + ": expected " + data[i + 3 - 100 + 65400] + ", got "
                        + value + ".");
            }
        }
    }

    @Test
    public void testReadMoreDataThanAvailable() throws IOException {
        final byte[] packet = {
            1, 1, 2, 3
        };

        final HttpInputStream in = this.getStream(packet);

        final byte[] data = new byte[10];
        for (int i = 0; i < data.length; ++i) {
            data[i] = 0;
        }

        final int length = in.read(data);

        assertThat("The number of bytes read is the number of available bytes.",
                   length,
                   is(4));

        assertThat("The data is correct.",
                   data,
                   is(equalTo(new byte[] {1, 1, 2, 3, 0, 0, 0, 0, 0, 0})));
    }

    @Test(expected = NullPointerException.class)
    public void testReadInvalidBuffer() throws IOException {
        final HttpInputStream in = this.getStream(this.dummyPacket);

        in.read(null, 0, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadInvalidOffset() throws IOException {
        final HttpInputStream in = this.getStream(this.dummyPacket);

        final byte[] buffer = new byte[20];
        in.read(buffer, -20, 20);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadNegativeLength() throws IOException {
        final HttpInputStream in = this.getStream(this.dummyPacket);

        final byte[] buffer = new byte[20];
        in.read(buffer, 0, -20);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadLongLength() throws IOException {
        final HttpInputStream in = this.getStream(this.dummyPacket);

        final byte[] buffer = new byte[20];
        in.read(buffer, 0, 2020);
    }

    @Test
    public void testAvailable() throws IOException {
        final HttpInputStream in = this.getStream(this.dummyPacket);

        assertThat("There is at least some data directly available.",
                   in.available(),
                   is(greaterThan(0)));
    }

    @Test(expected = IOException.class)
    public void testReadFromClosedStream() throws IOException {
        final byte[] packet = {
            'd', 'a', 't', 'a'
        };

        final HttpInputStream in = this.getStream(packet);

        in.close();
        in.read();
    }

    @Override
    public HttpInputStream getStream(final byte[] packet) {
        final ByteArrayInputStream source = new ByteArrayInputStream(packet);
        return new HttpInputStream(source);
    }
}
