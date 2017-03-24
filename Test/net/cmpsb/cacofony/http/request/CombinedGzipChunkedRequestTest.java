package net.cmpsb.cacofony.http.request;

import net.cmpsb.cacofony.io.HttpInputStream;
import net.cmpsb.cacofony.io.StreamHelper;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author Luc Everse
 */
public class CombinedGzipChunkedRequestTest {
    @Test
    public void test() throws IOException {
        final byte[] packet = {
            'P', 'O', 'S', 'T', ' ', '/', ' ', 'H', 'T', 'T', 'P', '/', '1', '.', '1', '\r', '\n',
            'T', 'r', 'a', 'n', 's', 'f', 'e', 'r', '-', 'E', 'n', 'c', 'o', 'd', 'i', 'n', 'g',
                ':', 'g', 'z', 'i', 'p', ',', ' ', 'c', 'h', 'u', 'n', 'k', 'e', 'd', '\r', '\n',
            '\r', '\n',

            '5', '\r', '\n',
            0x1f, (byte) 0x8b, 0x08, 0x00, 0x3c,
            '\r', '\n',

            'A', '\r', '\n',
            0x35, (byte) 0xd5, 0x58, 0x02, 0x03,
            (byte) 0xf3, 0x48, (byte) 0xcd, (byte) 0xc9, (byte) 0xc9,
            '\r', '\n',

            '1', '6', '\r', '\n',
            (byte) 0xd7, 0x51, 0x70, 0x4e, 0x4c,
            (byte) 0xce, 0x4f, (byte) 0xcb, (byte) 0xcf, (byte) 0xab,
            0x54, (byte) 0xe4, 0x02, 0x00, 0x59,
            (byte) 0xfe, 0x35, 0x16, 0x11, 0x00,
            0x00, 0x00,
            '\r', '\n',

            '0', '\r', '\n',
            '\r', '\n'
        };

        final HttpInputStream stream = this.getStream(packet);

        final HeaderParser headerParser = new HeaderParser();
        final StreamHelper streamHelper = new StreamHelper();
        final RequestParser requestParser = new RequestParser(headerParser, streamHelper);
        final MutableRequest request = requestParser.parse(stream);

        final byte[] read = new byte[1024];
        final int length = request.getBody().read(read);

        final String string = new String(read, 0, length, StandardCharsets.UTF_8);

        assertThat("The read string is correct.",
                   string,
                   is(equalTo("Hello, Cacofony!\n")));
    }

    private HttpInputStream getStream(final byte[] packet) {
        final ByteArrayInputStream source = new ByteArrayInputStream(packet);
        return new HttpInputStream(source);
    }
}
