package net.cmpsb.cacofony.io;

import net.cmpsb.cacofony.http.request.HeaderParser;
import net.cmpsb.cacofony.http.request.MutableRequest;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Tests that combine the chunked input and output streams.
 *
 * @author Luc Everse
 */
public class ChunkedStreamRoundtripTest {
    @Test
    public void testBigDataSet() throws IOException {
        final int dataSize = 9000;

        final Random random = new Random();
        final int[] bigData = random.ints(dataSize).map(i -> i & 255).toArray();
        final byte[] data = new byte[bigData.length];

        for (int i = 0; i < bigData.length; ++i) {
            data[i] = (byte) bigData[i];
        }

        final ByteArrayOutputStream outTarget = new ByteArrayOutputStream();
        final ChunkedOutputStream out = new ChunkedOutputStream(outTarget);

        out.write(data);
        out.close();

        final ByteArrayInputStream inSource = new ByteArrayInputStream(outTarget.toByteArray());
        final HttpInputStream httpIn = new HttpInputStream(inSource);
        final MutableRequest request = new MutableRequest();
        final HeaderParser headerParser = new HeaderParser();
        final ChunkedInputStream in = new ChunkedInputStream(httpIn, request, headerParser);

        final byte[] readData = new byte[dataSize];
        final int readLength = in.read(readData);

        assertThat("The read length is equal to the input length.",
                   readLength,
                   is(equalTo(dataSize)));

        assertThat("The read data is correct.",
                   readData,
                   is(equalTo(data)));
    }
}
