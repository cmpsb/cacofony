package net.wukl.cacofony.http2;

import net.wukl.cacofony.http.request.Header;
import net.wukl.cacofony.http2.huffman.Huffman;
import org.assertj.core.presentation.HexadecimalRepresentation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HpackTest {
    private static final byte[] EXAMPLE_2_1_PAYLOAD = new byte[] {
            (byte) 0x40, (byte) 0x0a, (byte) 0x63, (byte) 0x75, (byte) 0x73, (byte) 0x74,
            (byte) 0x6f, (byte) 0x6d, (byte) 0x2d, (byte) 0x6b, (byte) 0x65, (byte) 0x79,
            (byte) 0x0d, (byte) 0x63, (byte) 0x75, (byte) 0x73, (byte) 0x74, (byte) 0x6f,
            (byte) 0x6d, (byte) 0x2d, (byte) 0x68, (byte) 0x65, (byte) 0x61, (byte) 0x64,
            (byte) 0x65, (byte) 0x72
    };

    private static final List<Header> EXAMPLE_2_1_HEADERS = List.of(
            new Header("custom-key", "custom-header")
    );

    private static final byte[] EXAMPLE_2_2_PAYLOAD = new byte[] {
            (byte) 0x04, (byte) 0x0c, (byte) 0x2f, (byte) 0x73, (byte) 0x61, (byte) 0x6d,
            (byte) 0x70, (byte) 0x6c, (byte) 0x65, (byte) 0x2f, (byte) 0x70, (byte) 0x61,
            (byte) 0x74, (byte) 0x68,
    };

    private static final List<Header> EXAMPLE_2_2_HEADERS = List.of(
            new Header(":path", "/sample/path")
    );

    private static final byte[] EXAMPLE_2_3_PAYLOAD = new byte[] {
            (byte) 0x10, (byte) 0x08, (byte) 0x70, (byte) 0x61, (byte) 0x73, (byte) 0x73,
            (byte) 0x77, (byte) 0x6f, (byte) 0x72, (byte) 0x64, (byte) 0x06, (byte) 0x73,
            (byte) 0x65, (byte) 0x63, (byte) 0x72, (byte) 0x65, (byte) 0x74,
    };

    private static final List<Header> EXAMPLE_2_3_HEADERS = List.of(
            new Header("password", "secret")
    );

    private static final byte[] EXAMPLE_2_4_PAYLOAD = new byte[] {
            (byte) 0x82
    };

    private static final List<Header> EXAMPLE_2_4_HEADERS = List.of(
            new Header(":method", "GET")
    );

    private static final byte[] EXAMPLE_3_1_PAYLOAD = new byte[] {
            (byte) 0x82, (byte) 0x86, (byte) 0x84, (byte) 0x41, (byte) 0x0f, (byte) 0x77,
            (byte) 0x77, (byte) 0x77, (byte) 0x2e, (byte) 0x65, (byte) 0x78, (byte) 0x61,
            (byte) 0x6d, (byte) 0x70, (byte) 0x6c, (byte) 0x65, (byte) 0x2e, (byte) 0x63,
            (byte) 0x6f, (byte) 0x6d,
    };

    private static final List<Header> EXAMPLE_3_1_HEADERS = List.of(
            new Header(":method", "GET"),
            new Header(":scheme", "http"),
            new Header(":path", "/"),
            new Header(":authority", "www.example.com")
    );

    private static final byte[] EXAMPLE_3_2_PAYLOAD = new byte[] {
            (byte) 0x82, (byte) 0x86, (byte) 0x84, (byte) 0xbe, (byte) 0x58, (byte) 0x08,
            (byte) 0x6e, (byte) 0x6f, (byte) 0x2d, (byte) 0x63, (byte) 0x61, (byte) 0x63,
            (byte) 0x68, (byte) 0x65,
    };

    private static final List<Header> EXAMPLE_3_2_HEADERS = List.of(
            new Header(":method", "GET"),
            new Header(":scheme", "http"),
            new Header(":path", "/"),
            new Header(":authority", "www.example.com"),
            new Header("cache-control", "no-cache")
    );

    private static final byte[] EXAMPLE_3_3_PAYLOAD = new byte[] {
            (byte) 0x82, (byte) 0x87, (byte) 0x85, (byte) 0xbf, (byte) 0x40, (byte) 0x0a,
            (byte) 0x63, (byte) 0x75, (byte) 0x73, (byte) 0x74, (byte) 0x6f, (byte) 0x6d,
            (byte) 0x2d, (byte) 0x6b, (byte) 0x65, (byte) 0x79, (byte) 0x0c, (byte) 0x63,
            (byte) 0x75, (byte) 0x73, (byte) 0x74, (byte) 0x6f, (byte) 0x6d, (byte) 0x2d,
            (byte) 0x76, (byte) 0x61, (byte) 0x6c, (byte) 0x75, (byte) 0x65,
    };

    private static final List<Header> EXAMPLE_3_3_HEADERS = List.of(
            new Header(":method", "GET"),
            new Header(":scheme", "https"),
            new Header(":path", "/index.html"),
            new Header(":authority", "www.example.com"),
            new Header("custom-key", "custom-value")
    );

    private static final byte[] EXAMPLE_4_1_PAYLOAD = new byte[] {
            (byte) 0x82, (byte) 0x86, (byte) 0x84, (byte) 0x41, (byte) 0x8c, (byte) 0xf1,
            (byte) 0xe3, (byte) 0xc2, (byte) 0xe5, (byte) 0xf2, (byte) 0x3a, (byte) 0x6b,
            (byte) 0xa0, (byte) 0xab, (byte) 0x90, (byte) 0xf4, (byte) 0xff,
    };

    private static final List<Header> EXAMPLE_4_1_HEADERS = List.of(
            new Header(":method", "GET"),
            new Header(":scheme", "http"),
            new Header(":path", "/"),
            new Header(":authority", "www.example.com")
    );

    private static final byte[] EXAMPLE_4_2_PAYLOAD = new byte[] {
            (byte) 0x82, (byte) 0x86, (byte) 0x84, (byte) 0xbe, (byte) 0x58, (byte) 0x86,
            (byte) 0xa8, (byte) 0xeb, (byte) 0x10, (byte) 0x64, (byte) 0x9c, (byte) 0xbf,
    };

    private static final List<Header> EXAMPLE_4_2_HEADERS = List.of(
            new Header(":method", "GET"),
            new Header(":scheme", "http"),
            new Header(":path", "/"),
            new Header(":authority", "www.example.com"),
            new Header("cache-control", "no-cache")
    );

    private static final byte[] EXAMPLE_4_3_PAYLOAD = new byte[] {
            (byte) 0x82, (byte) 0x87, (byte) 0x85, (byte) 0xbf, (byte) 0x40, (byte) 0x88,
            (byte) 0x25, (byte) 0xa8, (byte) 0x49, (byte) 0xe9, (byte) 0x5b, (byte) 0xa9,
            (byte) 0x7d, (byte) 0x7f, (byte) 0x89, (byte) 0x25, (byte) 0xa8, (byte) 0x49,
            (byte) 0xe9, (byte) 0x5b, (byte) 0xb8, (byte) 0xe8, (byte) 0xb4, (byte) 0xbf,
    };

    private static final List<Header> EXAMPLE_4_3_HEADERS = List.of(
            new Header(":method", "GET"),
            new Header(":scheme", "https"),
            new Header(":path", "/index.html"),
            new Header(":authority", "www.example.com"),
            new Header("custom-key", "custom-value")
    );

    private Hpack hpack;

    @BeforeEach
    public void before() {
        final Huffman huffman = new Huffman();
        this.hpack = new Hpack(huffman);
    }

    @Test
    public void testDecodeExample2_1() {
        final var headers = this.hpack.read(EXAMPLE_2_1_PAYLOAD);
        assertThat(headers).containsExactlyInAnyOrderElementsOf(EXAMPLE_2_1_HEADERS);
    }

    @Test
    public void testDecodeExample2_2() {
        final var headers = this.hpack.read(EXAMPLE_2_2_PAYLOAD);
        assertThat(headers).containsExactlyInAnyOrderElementsOf(EXAMPLE_2_2_HEADERS);
    }

    @Test
    public void testDecodeExample2_3() {
        final var headers = this.hpack.read(EXAMPLE_2_3_PAYLOAD);
        assertThat(headers).containsExactlyInAnyOrderElementsOf(EXAMPLE_2_3_HEADERS);
    }

    @Test
    public void testDecodeExample2_4() {
        final var headers = this.hpack.read(EXAMPLE_2_4_PAYLOAD);
        assertThat(headers).containsExactlyInAnyOrderElementsOf(EXAMPLE_2_4_HEADERS);
    }

    @Test
    public void testEncodeExample2_4() {
        final var bytes = this.hpack.compress(EXAMPLE_2_4_HEADERS);
        assertThat(bytes)
                .withRepresentation(HexadecimalRepresentation.HEXA_REPRESENTATION)
                .isEqualTo(EXAMPLE_2_4_PAYLOAD);
    }

    @Test
    public void testDecodeExample3_1() {
        final var headers = this.hpack.read(EXAMPLE_3_1_PAYLOAD);
        assertThat(headers).containsExactlyInAnyOrderElementsOf(EXAMPLE_3_1_HEADERS);
    }

    @Test
    public void testDecodeExample3_2() {
        this.hpack.read(EXAMPLE_3_1_PAYLOAD);
        final var headers = this.hpack.read(EXAMPLE_3_2_PAYLOAD);
        assertThat(headers).containsExactlyInAnyOrderElementsOf(EXAMPLE_3_2_HEADERS);
    }

    @Test
    public void testDecodeExample3_3() {
        this.hpack.read(EXAMPLE_3_1_PAYLOAD);
        this.hpack.read(EXAMPLE_3_2_PAYLOAD);
        final var headers = this.hpack.read(EXAMPLE_3_3_PAYLOAD);
        assertThat(headers).containsExactlyInAnyOrderElementsOf(EXAMPLE_3_3_HEADERS);
    }

    @Test
    public void testDecodeExample4_1() {
        final var headers = this.hpack.read(EXAMPLE_4_1_PAYLOAD);
        assertThat(headers).containsExactlyInAnyOrderElementsOf(EXAMPLE_4_1_HEADERS);
    }

    @Test
    public void testEncodeExample4_1() {
        final var bytes = this.hpack.compress(EXAMPLE_4_1_HEADERS);
        assertThat(bytes)
                .withRepresentation(HexadecimalRepresentation.HEXA_REPRESENTATION)
                .isEqualTo(EXAMPLE_4_1_PAYLOAD);
    }

    @Test
    public void testDecodeExample4_2() {
        this.hpack.read(EXAMPLE_4_1_PAYLOAD);
        final var headers = this.hpack.read(EXAMPLE_4_2_PAYLOAD);
        assertThat(headers).containsExactlyInAnyOrderElementsOf(EXAMPLE_4_2_HEADERS);
    }

    @Test
    public void testEncodeExample4_2() {
        this.hpack.compress(EXAMPLE_4_1_HEADERS);
        final var bytes = this.hpack.compress(EXAMPLE_4_2_HEADERS);
        assertThat(bytes)
                .withRepresentation(HexadecimalRepresentation.HEXA_REPRESENTATION)
                .isEqualTo(EXAMPLE_4_2_PAYLOAD);
    }

    @Test
    public void testDecodeExample4_3() {
        this.hpack.read(EXAMPLE_4_1_PAYLOAD);
        this.hpack.read(EXAMPLE_4_2_PAYLOAD);
        final var headers = this.hpack.read(EXAMPLE_4_3_PAYLOAD);
        assertThat(headers).containsExactlyInAnyOrderElementsOf(EXAMPLE_4_3_HEADERS);
    }

    @Test
    public void testEncodeExample4_3() {
        this.hpack.compress(EXAMPLE_4_1_HEADERS);
        this.hpack.compress(EXAMPLE_4_2_HEADERS);
        final var bytes = this.hpack.compress(EXAMPLE_4_3_HEADERS);
        assertThat(bytes)
                .withRepresentation(HexadecimalRepresentation.HEXA_REPRESENTATION)
                .isEqualTo(EXAMPLE_4_3_PAYLOAD);
    }
}
