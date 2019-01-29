package net.wukl.cacofony.http2.huffman;

import org.assertj.core.presentation.HexadecimalRepresentation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class HuffmanTest {
    private Huffman huffman;

    @BeforeEach
    public void before() {
        this.huffman = new Huffman();
    }

    @Test
    public void testRoundtrip() {
        final var str = "Test string for testing!\n";

        final var encoded = this.huffman.encode(str);
        final var decoded = this.huffman.decode(encoded);

        assertThat(decoded).isEqualTo(str);
    }

    @Test
    public void testBinaryRoundtrip() throws IOException {
        try (var in = this.getFile("kilobyte-string")) {
            final var bytes = in.readAllBytes();
            final var binString = new String(bytes, StandardCharsets.ISO_8859_1);
            final var encoded = this.huffman.encode(binString);
            final var decoded = this.huffman.decode(encoded);
            assertThat(decoded).isEqualTo(binString);
        }
    }

    @Test
    public void testLargeBinaryRoundtrip() throws IOException {
        try (var in = this.getFile("16k-string")) {
            final var bytes = in.readAllBytes();
            final var binString = new String(bytes, StandardCharsets.ISO_8859_1);
            final var encoded = this.huffman.encode(binString);
            final var decoded = this.huffman.decode(encoded);
            assertThat(decoded).isEqualTo(binString);
        }
    }

    @Test
    public void testDecodeRfc7541Example1() {
        final var bytes = new byte[] {
                (byte) 0xf1, (byte) 0xe3, (byte) 0xc2, (byte) 0xe5, (byte) 0xf2, (byte) 0x3a,
                (byte) 0x6b, (byte) 0xa0, (byte) 0xab, (byte) 0x90, (byte) 0xf4, (byte) 0xff
        };

        final var decoded = this.huffman.decode(bytes);

        assertThat(decoded).isEqualTo("www.example.com");
    }

    @Test
    public void testDecodeRfc7541Example2() {
        final var bytes = new byte[] {
                (byte) 0xa8, (byte) 0xeb, (byte) 0x10, (byte) 0x64, (byte) 0x9c, (byte) 0xbf
        };

        final var decoded = this.huffman.decode(bytes);

        assertThat(decoded).isEqualTo("no-cache");
    }

    @Test
    public void testEncodeRfc7451Example1() {
        final var str = "www.example.com";
        final var encoded = this.huffman.encode(str);

        assertThat(encoded)
                .withRepresentation(new HexadecimalRepresentation())
                .containsExactly(
                        0xf1, 0xe3, 0xc2, 0xe5, 0xf2, 0x3a, 0x6b, 0xa0, 0xab, 0x90, 0xf4, 0xff
                );
    }

    @Test
    public void testEncodeRfc7451Example2() {
        final var encoded = this.huffman.encode("no-cache");

        assertThat(encoded)
                .withRepresentation(new HexadecimalRepresentation())
                .containsExactly(0xa8, 0xeb, 0x10, 0x64, 0x9c, 0xbf);
    }

    /**
     * Loads a file from the test class' resource folder.
     *
     * @param name the name of the resource to load
     *
     * @return the stream or {@code null} if the file could not be found
     */
    private InputStream getFile(final String name) {
        return HuffmanTest.class.getResourceAsStream("/net/wukl/cacofony/http2/huffman/" + name);
    }
}
