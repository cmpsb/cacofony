package net.wukl.cacofony.http2.huffman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A class implementing Huffman encoding and decoding according to RFC 7541.
 */
public class Huffman {
    /**
     * The path to the table file.
     */
    private static final String TABLE_PATH = "/net/wukl/cacofony/http2/huffman-table.txt";

    /**
     * The pattern used to extract entries in the table from RFC 7541 appendix B.
     */
    private static final Pattern ROW_PATTERN = Pattern.compile(
        "\\s*(... )?\\(\\s*(?<val>\\d+)\\)\\s*(\\|[10]+)+"
        + "\\s*(?<seq>[0-9a-f]+)\\s*\\[\\s*(?<len>\\d+)\\]"
    );

    /**
     * The encoding table.
     */
    private static final BitSpan[] ENCODING_TABLE = new BitSpan[257];

    /**
     * The decoding table.
     */
    private static final Map<BitSpan, Integer> DECODING_TABLE = new HashMap<>();

    /**
     * A special value in the tree indicating the encoded string ends.
     */
    private static final int EOS = 256;

    /**
     * The EOS code.
     */
    private static final BitSpan EOS_CODE;

    /**
     * The eight most significant bits of the EOS code.
     */
    private static final byte EOS_BYTE;

    /**
     * The maximum padding length after a Huffman-encoded string.
     */
    private static final int MAX_PADDING = 7;

    /**
     * The minimum number of bits a single encoded character may span.
     */
    private static final int MIN_CODE_LENGTH = 5;

    /**
     * The maximum number of bits a single encoded character may span.
     */
    private static final int MAX_CODE_LENGTH = 31;

    /**
     * The expected number of bits used per character.
     */
    private static final int EXPECTED_BITS_PER_CHAR = 6;

    static {
        // Preprocess the Huffman table
        try (var in = new BufferedReader(new InputStreamReader(
                Huffman.class.getResourceAsStream(TABLE_PATH)
        ))) {
            for (;;) {
                final var line = in.readLine();
                if (line == null) {
                    break;
                }

                final var matcher = ROW_PATTERN.matcher(line);
                if (!matcher.matches()) {
                    continue;
                }

                final var valueStr = matcher.group("val");
                final var value = Integer.parseInt(valueStr);

                final var seqStr = matcher.group("seq");
                final var lenStr = matcher.group("len");

                final var span = new BitSpan(Integer.parseInt(lenStr), Long.parseLong(seqStr, 16));

                ENCODING_TABLE[value] = span;
                DECODING_TABLE.put(span, value);
            }
        } catch (final IOException e) {
            throw new HuffmanInitializationException("Unable to locate the Huffman table", e);
        }

        EOS_CODE = ENCODING_TABLE[EOS];
        EOS_BYTE = (byte) (EOS_CODE.bits >>> (EOS_CODE.length - Byte.SIZE));
    }

    /**
     * Decodes a series of bytes using the Huffman decoding algorithm from RFC 7541 appendix B.
     *
     * @param data the set of bytes to decode
     * @param charset the character set to use for constructing the string
     *
     * @return the string, without any end-of-string characters
     *
     * @throws DecodingException if the bits do not correctly encode a string
     */
    public String decode(final byte[] data, final Charset charset) {
        var string = new byte[data.length * Byte.SIZE / EXPECTED_BITS_PER_CHAR];
        int c = 0;

        boolean decoding = true;
        int b = 0;
        final var seq = new BitSpan(0, 0);

        while (decoding && b < data.length * Byte.SIZE) {
            final var remainder = Byte.SIZE - (b % Byte.SIZE);

            // If the bit string ends before a full minimum code can be read, check for EOS.
            if (b + MIN_CODE_LENGTH >= data.length * Byte.SIZE) {
                seq.bits = data[b / Byte.SIZE] & ((1 << remainder) - 1);
                if (seq.bits == EOS_CODE.bits >>> (EOS_CODE.length - remainder)) {
                    break;
                }

                throw new DecodingException("Data truncated");
            }

            // Check if the minimum code fits in the current byte
            if (remainder >= MIN_CODE_LENGTH) {
                seq.bits = (data[b / Byte.SIZE] & 0xFF)
                        >>> ((Byte.SIZE - MIN_CODE_LENGTH) - (Byte.SIZE - remainder));
                b += MIN_CODE_LENGTH;
            } else {
                // Otherwise read the remaining part out of the next byte
                seq.bits = ((data[b / Byte.SIZE] & 0xFF) << (MIN_CODE_LENGTH - remainder)) & 0xFF;
                b += remainder;
                seq.bits |= (data[b / Byte.SIZE] & 0xFF)
                        >>> ((Byte.SIZE - MIN_CODE_LENGTH) + remainder);
                b += MIN_CODE_LENGTH - remainder;
            }

            // Normalize the code to the minimum length
            seq.bits &= (1 << MIN_CODE_LENGTH) - 1;
            seq.length = MIN_CODE_LENGTH;

            for (int len = MIN_CODE_LENGTH; len <= MAX_CODE_LENGTH; ++len) {
                final var ch = DECODING_TABLE.get(seq);
                if (ch != null) {
                    if (ch.equals(EOS)) {
                        throw new DecodingException("Stray EOS character");
                    }

                    if (c >= string.length) {
                        final var newString = new byte[
                                string.length
                                + (data.length * Byte.SIZE - b) / EXPECTED_BITS_PER_CHAR
                                + 1
                        ];
                        System.arraycopy(string, 0, newString, 0, string.length);
                        string = newString;
                    }

                    string[c] = ch.byteValue();
                    ++c;
                    break;
                }

                // The code was not found, check if the bit string ends next
                // or if the code is invalid
                if (b >= data.length * Byte.SIZE) {
                    if (seq.bits == EOS_CODE.bits >>> (EOS_CODE.length - seq.length)) {
                        decoding = false;
                        break;
                    }

                    throw new DecodingException("Data truncated");
                }

                if (len >= MAX_CODE_LENGTH) {
                    throw new DecodingException("Invalid bit sequence");
                }

                // Grab the next bit and increase the code size
                seq.bits <<= 1;
                seq.bits |= ((data[b / Byte.SIZE] & 0xFF)
                        >>> (Byte.SIZE - (b % Byte.SIZE)) - 1) & 1;
                ++seq.length;
                ++b;
            }
        }

        return new String(string, 0, c, charset);
    }

    /**
     * Decodes a series of bytes using the Huffman decoding algorithm from RFC 7541 appendix B.
     *
     * The character set is assumed to be UTF-8.
     *
     * @param data the set of bytes to decode
     *
     * @return the string, without any end-of-string characters
     *
     * @throws DecodingException if the bits do not correctly encode a string
     */
    public String decode(final byte[] data) {
        return this.decode(data, StandardCharsets.UTF_8);
    }

    /**
     * Encodes a string using the Huffman encoding algorithm from RFC 7541 appendix B.
     *
     * @param str the string to encode
     * @param charset the character set to use for transforming the string into octets
     *
     * @return the bit string encoding the string
     */
    public byte[] encode(final String str, final Charset charset) {
        var bytes = new byte[str.length() * EXPECTED_BITS_PER_CHAR / Byte.SIZE];

        final var chars = str.getBytes(charset);
        int b = 0;
        for (int i = 0; i < chars.length; ++i) {
            final var ch = chars[i] & 0xFF;
            final var sequence = ENCODING_TABLE[ch];
            if (b + sequence.length > bytes.length * Byte.SIZE) {
                final var newBytes = new byte[
                    bytes.length
                    + (sequence.length + (chars.length - i)
                            * EXPECTED_BITS_PER_CHAR + Byte.SIZE - 1)
                            / Byte.SIZE
                    + 1
                ];
                System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
                bytes = newBytes;
            }

            final var end = b + sequence.length;
            final var excessBits = b % Byte.SIZE;
            if (excessBits != 0) {
                final var remainder = Byte.SIZE - excessBits;
                final var distance = sequence.length - remainder;
                if (remainder < sequence.length) {
                    bytes[b / Byte.SIZE] |= sequence.bits >>> distance;
                    b += remainder;
                } else {
                    bytes[b / Byte.SIZE] |= sequence.bits << -distance;
                    b += sequence.length;
                    continue;
                }
            }

            while (b + Byte.SIZE < end) {
                bytes[b / Byte.SIZE] = (byte) (sequence.bits >>> (end - b - Byte.SIZE));
                b += Byte.SIZE;
            }

            final var remainder = end - b;
            if (remainder > 0) {
                bytes[b / Byte.SIZE] = (byte) (sequence.bits << (Byte.SIZE - remainder));
                b += remainder;
            }
        }

        final var remainder = b % Byte.SIZE;
        bytes[b / 8] |= (EOS_BYTE & 0xFF) >>> remainder;

        b += Byte.SIZE - remainder;

        final var finalBytes = new byte[b / Byte.SIZE];
        System.arraycopy(bytes, 0, finalBytes, 0, b / Byte.SIZE);

        return finalBytes;
    }

    /**
     * Encodes a string using the Huffman encoding algorithm from RFC 7541 appendix B.
     *
     * The character set is assumed to be UTF-8.
     *
     * @param str the string to encode
     *
     * @return the bit string encoding the string
     */
    public byte[] encode(final String str) {
        return this.encode(str, StandardCharsets.UTF_8);
    }

    /**
     * A span of bits in the Huffman table.
     */
    private static final class BitSpan {
        /**
         * The number of bits in this span.
         */
        private int length;

        /**
         * The bits themselves.
         */
        private long bits;

        /**
         * Creates a new bit span.
         *
         * @param length the number of bits in this span
         * @param bits the bits themselves
         */
        private BitSpan(final int length, final long bits) {
            this.length = length;
            this.bits = bits;
        }

        /**
         * Checks whether this span is equal to another.
         *
         * @param obj the other object
         *
         * @return {@code true} if the objects are equal, {@code false} otherwise
         */
        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof BitSpan)) {
                return false;
            }

            final var other = (BitSpan) obj;

            return other.length == this.length && other.bits == this.bits;
        }

        /**
         * Calculates the span's hash code.
         *
         * @return the hash code
         */
        @Override
        public int hashCode() {
            return Objects.hash(this.length, this.bits);
        }
    }
}
