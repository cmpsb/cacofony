package net.cmpsb.cacofony.io;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Luc Everse
 */
public class HttpInputStreamTest {
    @Test
    public void testReadLine() throws IOException {
        final byte[] packet = {
            'F', 'i', 'r', 's', 't', ' ', 'l', 'i', 'n', 'e', '\r', '\n',
            'S', 'e', 'c', 'o', 'n', 'd',  '\r', '\n'
        };

        final ByteArrayInputStream source = new ByteArrayInputStream(packet);
        final HttpInputStream in = new HttpInputStream(source);

        final String first = in.readLine();
        final String second = in.readLine();
        final String empty = in.readLine();

        assertThat("The first string has been read correctly.",
                   first,
                   is(equalTo("First line")));

        assertThat("The second string has been read correctly.",
                   second,
                   is(equalTo("Second")));

        assertThat("The final string is empty.",
                   empty.isEmpty(),
                   is(true));
    }
}
