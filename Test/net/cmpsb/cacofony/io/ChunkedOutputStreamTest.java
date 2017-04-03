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
    public void testWriteAcrossChunks() throws IOException {
        final ChunkedOutputStream out = new ChunkedOutputStream(this.target, 5);
        out.write(new byte[] {1, 2, 3, 4, 5, 6, 7});
        out.close();

        final byte[] expected = {
            '5', '\r', '\n',
            1, 2, 3, 4, 5,
            '\r', '\n',

            '2', '\r', '\n',
            6, 7,
            '\r', '\n',

            '0', '\r', '\n',
            '\r', '\n'
        };

        assertThat("The output buffer is as expected.",
                   this.target.toByteArray(),
                   is(equalTo(expected)));
    }
}
