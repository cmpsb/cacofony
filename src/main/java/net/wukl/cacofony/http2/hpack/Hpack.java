package net.wukl.cacofony.http2.hpack;

import net.wukl.cacofony.http.request.Header;
import net.wukl.cacofony.http2.hpack.huffman.Huffman;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An HPACK (RFC 7541) encoder and decoder.
 */
public class Hpack {
    private static final Logger logger = LoggerFactory.getLogger(Hpack.class);

    /**
     * The static header table.
     */
    private static final TableEntry[] STATIC_TABLE = new TableEntry[62];

    /**
     * The inverse static table mapping entries to their positions in the static table.
     */
    private static final Map<TableEntry, Integer> INV_STATIC_TABLE = new HashMap<>();

    /**
     * The number of entries in the static table, excluding the invalid null entry.
     */
    private static final int STATIC_TABLE_LENGTH = 62;

    /**
     * The number of bits available in an octet extending an integer.
     */
    private static final int EXTENDED_INTEGER_BITS = 7;

    /**
     * The bit used to indicate a continuation of an extended integer.
     */
    private static final int EXTENSION_CONTINUATION_MASK = 0b1000_0000;

    /**
     * The pattern used to parse a row in the static table source file.
     */
    private static final Pattern ROW_PATTERN = Pattern.compile(
            "[\\s|]*(?<index>\\d+)[\\s|]*(?<key>[a-z\\-0-9:]+)[\\s|]*(?<value>[^|]+)?.*"
    );

    static {
        try {
            try (var in = new BufferedReader(new InputStreamReader(
                    Hpack.class.getResourceAsStream("hpack-static-table.txt")
            ))) {
                while (true) {
                    final var line = in.readLine();
                    if (line == null) {
                        break;
                    }

                    final var matcher = ROW_PATTERN.matcher(line);
                    if (!matcher.matches()) {
                        continue;
                    }

                    final var indexStr = matcher.group("index").trim();
                    final var key = matcher.group("key").trim();
                    var nvalue = matcher.group("value");
                    final String value;
                    if (nvalue == null) {
                        value = null;
                    } else {
                        value = nvalue.trim();
                    }

                    final var index = Integer.parseInt(indexStr);
                    final var entry = new TableEntry(key, value, 0);
                    STATIC_TABLE[index] = entry;
                    INV_STATIC_TABLE.put(entry, index);
                }
            }
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * The dynamic table generated while decompressing headers.
     */
    private final DynamicTable decompressionTable = new DynamicTable();

    /**
     * The dynamic table generated while compressing headers.
     */
    private final DynamicTable compressionTable = new DynamicTable();

    /**
     * The Huffman codec to use.
     */
    private final Huffman huffman;

    /**
     * The maximum size of the dynamic table connection-wide.
     */
    private int maxDynDecodingSize = 4096;

    /**
     * The maximum size of the encoding dynamic table connection-wide.
     */
    private int maxDynEncodingSize = 4096;

    /**
     * Creates a new HPACK codec.
     *
     * @param huffman the Huffman codec to use for reading and writing compressed strings
     */
    public Hpack(final Huffman huffman) {
        this.huffman = huffman;

        this.decompressionTable.resize(this.maxDynDecodingSize);
        this.compressionTable.resize(this.maxDynEncodingSize);
    }

    /**
     * Reads a list of headers from the byte stream.
     *
     * This function reads until all bytes have been consumed, and can therefore not be called
     * on partial frames.
     *
     * @param data the byte stream
     *
     * @return the set of headers
     */
    public List<Header> decompress(final byte[] data) {
        final var headers = new HeaderSet();

        for (int i = 0; i < data.length;) {
            final var lead = data[i];
            if (this.isIndexedHeaderField(lead)) {
                i = this.readIndexedHeaderField(data, i, headers);
            } else if (this.isLiteralHeaderFieldWithIncrementalIndexing(lead)) {
                i = this.readLiteralHeader(data, i, 6, headers, true);
            } else if (this.isLiteralHeaderFieldWithoutIndexing(lead)) {
                i = this.readLiteralHeader(data, i, 4, headers, false);
            } else if (this.isLiteralHeaderFieldNeverIndexed(lead)) {
                i = this.readLiteralHeader(data, i, 4, headers, false);
            } else if (this.isDynamicTableSizeUpdate(lead)) {
                final var parsedSize = this.readInteger(data, i, 3);
                i = parsedSize.index;
                final var size = parsedSize.value.intValue();

                if (size >= this.maxDynDecodingSize) {
                    throw new HpackDecodingException(
                            "New dynamic table size exceeds connection limit"
                    );
                }

                this.decompressionTable.resize(parsedSize.value.intValue());
            } else {
                throw new HpackDecodingException("Unrecognized header field encoding");
            }
        }

        return headers.toList();
    }

    /**
     * Compresses a set of headers into a byte stream.
     *
     * @param headers the headers to compress
     *
     * @return the bytes consisting of the compressed headers
     */
    public byte[] compress(final List<Header> headers) {
        try (var bytes = new ByteArrayOutputStream()) {

            final var entries = headers.stream()
                    .flatMap(h -> h.getValues().stream().map(v ->
                            new TableEntry(h.getKey(), v, h.isSensitive())
                    ))
                    .collect(Collectors.toList());

            for (final var entry : entries) {
                final var index = this.getIndexInTable(entry);
                if (index != 0) {
                    bytes.write(this.writeInt(0b1000_0000, 7, index));
                    continue;
                }

                final int prefix;
                final int prefixLength;
                if (entry.isSensitive) {
                    prefix = 0b0001_0000;
                    prefixLength = 4;
                } else {
                    prefix = 0b0100_0000;
                    prefixLength = 6;
                }

                final var partialIndex = this.getIndexInTable(entry.getKeyEntry());
                bytes.write(this.writeInt(prefix, prefixLength, partialIndex));
                if (partialIndex == 0) {
                    this.writeString(entry.key, bytes);
                }

                this.writeString(entry.value, bytes);

                if (!entry.isSensitive) {
                    this.compressionTable.insert(entry);
                }
            }

            return bytes.toByteArray();
        } catch (final IOException ex) {
            // Never happens. Still, wrap the exception and rethrow just to be sure.
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns the index of a table entry.
     *
     * If the entry is not present in any table, 0 is returned instead.
     *
     * @param entry the entry to look for
     *
     * @return the index, or 0 if the entry is not present
     */
    private int getIndexInTable(final TableEntry entry) {
        final var staticIndex = INV_STATIC_TABLE.get(entry);
        if (staticIndex != null) {
            return staticIndex;
        }

        return this.compressionTable.indexOf(entry);
    }

    /**
     * Writes an integer into the byte string.
     *
     * @param lead the leading bits that are not part of the integer
     * @param prefixLength the number of bits the integer prefix
     * @param value the actual value to store
     *
     * @return the bytes representing the integer
     */
    private byte[] writeInt(final int lead, final int prefixLength, final int value) {
        final var initialMax = (1 << prefixLength) - 1;

        if (value < initialMax) {
            return new byte[] {(byte) ((lead | value) & 0xFF)};
        }

        if (value < initialMax + (1 << EXTENDED_INTEGER_BITS)) {
            return new byte[] {
                    (byte) ((lead | initialMax) & 0xFF),
                    (byte) ((value - initialMax) & 0xFF)
            };
        }

        final var extensionMask = (1 << EXTENDED_INTEGER_BITS) - 1;

        if (value < initialMax + (1 << (EXTENDED_INTEGER_BITS * 2))) {
            return new byte[] {
                    (byte) ((lead | initialMax) & 0xFF),
                    (byte) (((value - initialMax) & extensionMask) | EXTENSION_CONTINUATION_MASK),
                    (byte) (((value - initialMax) >> EXTENDED_INTEGER_BITS) & extensionMask)
            };
        }

        if (value < initialMax + (1 << (EXTENDED_INTEGER_BITS * 3))) {
            return new byte[] {
                    (byte) ((lead | initialMax) & 0xFF),
                    (byte) ((value - initialMax) & extensionMask),
                    (byte) ((((value - initialMax) >> EXTENDED_INTEGER_BITS) & extensionMask)
                            | EXTENSION_CONTINUATION_MASK),
                    (byte) (((value - initialMax) >> (2 * EXTENDED_INTEGER_BITS)) & extensionMask)
            };
        }

        throw new HpackEncodingException("Integer is too large: " + value);
    }

    /**
     * Writes a Huffman-encoded string with length to the byte stream.
     *
     * The string is encoded using UTF-8.
     *
     * @param str the string to write
     * @param out the byte array output stream to write the bytes to
     *
     * @throws IOException probably never
     */
    private void writeString(final String str, final ByteArrayOutputStream out) throws IOException {
        final var bytes = this.huffman.encode(str);

        out.write(this.writeInt(0b1000_0000, 7, bytes.length));
        out.write(bytes);
    }

    /**
     * Adjusts the maximum dynamic table size for decoding.
     *
     * @param newMaxSize the new maximum size
     * @param force if {@code true}, the maximum size is directly applied to the dynamic table,
     *              otherwise the value is set for future comparison
     */
    public void updateMaximumDecodingSize(final int newMaxSize, final boolean force) {
        this.maxDynDecodingSize = newMaxSize;

        if (force) {
            this.decompressionTable.resize(newMaxSize);
        }
    }

    /**
     * Adjusts the maximum dynamic table size for encoding.
     *
     * @param newMaxSize the new maximum size
     * @param force if {@code true}, the maximum size is directly applied to the dynamic table,
     *              otherwise the value is set for future comparison
     */
    public void updateMaximumEncodingSize(final int newMaxSize, final boolean force) {
        this.maxDynEncodingSize = newMaxSize;

        if (force) {
            this.compressionTable.resize(newMaxSize);
        }
    }

    /**
     * Tests whether the next field is an indexed header.
     *
     * @param octet the octet beginning the next field
     *
     * @return {@code true} if it is an indexed header field, {@code false} otherwise
     */
    private boolean isIndexedHeaderField(final long octet) {
        return (octet & 0b1000_0000) != 0;
    }

    /**
     * Tests whether the next field is a literal header to be added to the dynamic table.
     *
     * @param octet the octet beginning the next field
     *
     * @return {@code true} if it is a literal header field, {@code false} otherwise
     */
    private boolean isLiteralHeaderFieldWithIncrementalIndexing(final long octet) {
        return (octet & 0b1100_0000) == 0b0100_0000;
    }

    /**
     * Tests whether the next field is a literal header not to be added to the dynamic table.
     *
     * @param octet the octet beginning the next field
     *
     * @return {@code true} if it is a literal header field, {@code false} otherwise
     */
    private boolean isLiteralHeaderFieldWithoutIndexing(final long octet) {
        return (octet & 0b1111_0000) == 0b0000_0000;
    }

    /**
     * Tests whether the next field is a literal header never to be added to the dynamic table.
     *
     * @param octet the octet beginning the next field
     *
     * @return {@code true} if it is a literal header field, {@code false} otherwise
     */
    private boolean isLiteralHeaderFieldNeverIndexed(final long octet) {
        return (octet & 0b1111_0000) == 0b0001_0000;
    }

    /**
     * Tests whether the next field is a dynamic table size update.
     *
     * @param octet the octet beginning the next field
     *
     * @return {@code true} if it is a dynamic table size update, {@code false} otherwise
     */
    private boolean isDynamicTableSizeUpdate(final long octet) {
        return (octet & 0b1110_0000) == 0b0010_0000;
    }

    /**
     * Reads a fully indexed header field from the byte stream.
     *
     * @param data the bytes
     * @param ii the initial index to start reading bytes at
     * @param headers the set of headers to append the newly read header to
     *
     * @return the index past the read header field
     */
    private int readIndexedHeaderField(final byte[] data, final int ii, final HeaderSet headers) {
        var i = ii;
        final var parsedIndex = this.readInteger(data, i, 7);
        i = parsedIndex.index;

        final var header = this.getIndexedHeader(parsedIndex.value.intValue());

        if (header == null) {
            throw new HpackDecodingException("Index exceeds dynamic table");
        }

        headers.insert(header);

        return i;
    }

    /**
     * Reads a partially or fully literal header field from the byte stream.
     *
     * @param data the bytes
     * @param ii the initial index to start reading at
     * @param prefix the prefix length of the value index
     * @param headers the header set to append the newly read header to
     * @param addToTable if {@code true}, the new header is appended to the dynmic table,
     *                   if {@code false}, the header is not indexed
     *
     * @return the index past the read header field
     */
    private int readLiteralHeader(
            final byte[] data, final int ii, final int prefix,
            final HeaderSet headers, final boolean addToTable
    ) {
        var i = ii;

        final var parsedKeyIndex = this.readInteger(data, i, prefix);
        i = parsedKeyIndex.index;

        final String key;
        final int keyLength;
        if (parsedKeyIndex.value != 0) {
            final var entry = this.getIndexedHeader(parsedKeyIndex.value.intValue());
            key = entry.key;
            keyLength = entry.length;
        } else {
            final var parsedKey = this.readString(data, i);
            i = parsedKey.index;
            key = parsedKey.value;
            keyLength = parsedKey.length;
        }

        final var parsedValue = this.readString(data, i);
        i = parsedValue.index;
        final var value = parsedValue.value;

        headers.insert(key, value);

        if (addToTable) {
            this.decompressionTable.insert(
                    new TableEntry(key, value, keyLength + parsedValue.length + 32)
            );
        }

        return i;
    }

    /**
     * Returns an indexed header table entry from the static or dynamic table.
     *
     * @param index the index of the header
     *
     * @return the table entry
     *
     * @throws IndexOutOfBoundsException if the header is not present in either of the tables
     * @throws HpackDecodingException if the index is zero
     */
    private TableEntry getIndexedHeader(final int index) {
        if (index == 0) {
            throw new HpackDecodingException("Invalid index 0 entry");
        }

        if (index < STATIC_TABLE_LENGTH) {
            return STATIC_TABLE[index];
        }

        return this.decompressionTable.get(index);
    }

    /**
     * Reads a string from the byte stream.
     *
     * @param data the bytes
     * @param ii the initial index to start reading bytes at
     *
     * @return the string and the next index to continue reading at
     */
    private ParsedValue<String> readString(final byte[] data, final int ii) {
        var start = data[ii];

        final var parsedLength = this.readInteger(data, ii, 7);
        var i = parsedLength.index;
        final var length = parsedLength.value.intValue();

        if (i + length > data.length) {
            throw new HpackDecodingException("String is beyond buffer");
        }

        var bytes = new byte[length];
        System.arraycopy(data, i, bytes, 0, length);

        i += length;

        if ((start & 0b1000_0000) != 0) {
            return new ParsedValue<>(this.huffman.decode(bytes), i, length);
        }

        return new ParsedValue<>(new String(bytes, StandardCharsets.UTF_8), i, length);
    }

    /**
     * Parses an integer from a stream of bytes.
     *
     * @param data the stream of bytes to parse
     * @param ii the initial index to start reading bytes at
     * @param prefixLength the number of bits prefixing the number
     *
     * @return the parsed integer, containing the value and how many bytes were consumed
     */
    private ParsedValue<Long> readInteger(final byte[] data, final int ii, final int prefixLength) {
        var i = ii;
        final var mask = (1 << prefixLength) - 1;
        final var prefix = data[i] & mask;

        if (prefix < mask) {
            return new ParsedValue<>((long) prefix, i + 1, 1);
        }

        ++i;

        long value = 0;
        for (; i < data.length && (data[i] & EXTENSION_CONTINUATION_MASK) != 0; ++i) {
            value |= (data[i] & ~EXTENSION_CONTINUATION_MASK)
                             << ((i - ii - 1) * EXTENDED_INTEGER_BITS);
        }

        if (i >= data.length) {
            throw new HpackDecodingException("Continued integer truncated");
        }

        value |= data[i] << ((i - ii - 1) * EXTENDED_INTEGER_BITS);
        ++i;

        return new ParsedValue<>(value + prefix, i, i - ii);
    }

    /**
     * A value read from the byte stream.
     *
     * @param <T> the type of value this class contains
     */
    private static final class ParsedValue<T> {
        /**
         * The actual value.
         */
        private final T value;

        /**
         * The next index into the byte stream.
         */
        private final int index;

        /**
         * The number of bytes consumed to read the value.
         */
        private final int length;

        /**
         * Creates a new parsed value.
         *
         * @param value the value
         * @param index the next index into the byte stream
         * @param length the number of bytes composing the read value
         */
        private ParsedValue(final T value, final int index, final int length) {
            this.value = value;
            this.index = index;
            this.length = length;
        }
    }

    /**
     * The set of headers to return.
     */
    private static final class HeaderSet {
        /**
         * The actual map of header keys and header values.
         */
        private final Map<String, Header> headers = new HashMap<>();

        /**
         * Inserts a key and any number of values into the set.
         *
         * @param key the name of the header
         * @param values the values to insert for that header
         */
        private void insert(final String key, final List<String> values) {
            var header = this.headers.get(key);
            if (header == null) {
                header = new Header(key);
                this.headers.put(key, header);
            }

            header.getValues().addAll(values);
        }

        /**
         * Inserts a static or dynamic table entry into the header set.
         *
         * @param header the entry to add
         */
        private void insert(final TableEntry header) {
            this.insert(header.key, header.value);
        }

        /**
         * Inserts a key-value header pair into the header set.
         *
         * @param key the key of the header
         * @param value the value of the header
         */
        private void insert(final String key, final String value) {
            if (value == null) {
                throw new HpackDecodingException("Cannot insert a header without a value");
            }

            this.insert(key, List.of(value));
        }

        /**
         * Returns the set as a list of headers.
         *
         * @return the headers
         */
        private List<Header> toList() {
            return List.copyOf(this.headers.values());
        }
    }

    /**
     * An entry in the static or dynamic table.
     */
    private static final class TableEntry {
        /**
         * The name of the header.
         */
        private final String key;

        /**
         * The value of the header.
         */
        private final String value;

        /**
         * The size of the entry in octets.
         *
         * @see <a href="https://tools.ietf.org/html/rfc7541#section-4.1">RFC 7541 Section 4.1</a>
         */
        private final int length;

        /**
         * Whether the entry originated from a security-sensitive header.
         */
        private final boolean isSensitive;

        /**
         * Creates a new table entry.
         *
         * @param key the name of the header
         * @param value the value of the header
         * @param length the length of the header
         */
        private TableEntry(final String key, final String value, final int length) {
            this.key = key;
            this.value = value;
            this.length = length;
            this.isSensitive = false;
        }

        /**
         * Creates a new table entry and calculates the size of the entry.
         *
         * The size of the entry is based on the sum of the number of bytes the key and value
         * take together when encoded with UTF-8 plus 32 bytes of overhead as per
         * RFC 7541 Section 4.1.
         *
         * @param key the key
         * @param value the value
         * @param isSensitive whether the entry originated from a security-sensitive header
         */
        private TableEntry(final String key, final String value, final boolean isSensitive) {
            this.key = key;
            this.value = value;
            this.length = this.key.getBytes(StandardCharsets.UTF_8).length
                    + this.value.getBytes(StandardCharsets.UTF_8).length
                    + 32;
            this.isSensitive = isSensitive;
        }

        /**
         * Returns an entry without any value, for searching for indexed-key literals.
         *
         * @return the entry with the same key and the value set to {@code null}
         */
        private TableEntry getKeyEntry() {
            return new TableEntry(this.key, null, 0);
        }

        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof TableEntry)) {
                return false;
            }

            final var other = (TableEntry) obj;
            return this.key.equalsIgnoreCase(other.key) && Objects.equals(this.value, other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.key.toLowerCase(), this.value);
        }
    }

    /**
     * The dynamic table kept by the codec.
     */
    private static final class DynamicTable {
        /**
         * The actual collection of entries.
         *
         * Modeled as a stack since the algorithm behaves as a LIFO collection.
         */
        private final Stack<TableEntry> table = new Stack<>();

        /**
         * The current sum of all entry sizes in the table.
         */
        private int size = 0;

        /**
         * The current size of the table in RFC 7541 parlance.
         *
         * This is the upper limit, but can by dynamically changed through HPACK. The connection-set
         * maximum is at {@link Hpack#maxDynDecodingSize}.
         */
        private int capacity = 0;

        /**
         * Looks up an entry in the table.
         *
         * The index is absolute, so no static table offset correction is needed.
         *
         * @param index the index of the entry
         *
         * @return the entry or {@code null} if the entry does not exist
         */
        private TableEntry get(final int index) {
            if (index > this.table.size() + STATIC_TABLE_LENGTH) {
                throw new HpackDecodingException(
                        "Table index (" + index + ") outside of table space ("
                                + this.table.size() + " + " + STATIC_TABLE_LENGTH + ")"
                );
            }

            return this.table.get(this.table.size() - (index - STATIC_TABLE_LENGTH) - 1);
        }

        /**
         * Looks up the index of an entry in the table.
         *
         * The index includes the static table offset.
         *
         * If the entry is not found, 0 is returned instead.
         *
         * @param entry the entry to look for
         *
         * @return the index
         */
        private int indexOf(final TableEntry entry) {
            final var index = this.table.indexOf(entry);
            if (index != -1) {
                return (this.table.size() - index - 1) + STATIC_TABLE_LENGTH;
            }

            return 0;
        }

        /**
         * Inserts an entry into the table.
         *
         * If the table is full, then the most recent entries are discarded
         * until the new entry fits. Then, if the entire table is too small the entry itself
         * is discarded as well.
         *
         * @param entry the entry to insert
         *
         * @see <a href="https://tools.ietf.org/html/rfc7541#section-4.4">RFC 7541 Section 4.4</a>
         */
        private void insert(final TableEntry entry) {
            while (this.size + entry.length > this.capacity) {
                this.pop();
            }

            if (this.size + entry.length <= this.capacity) {
                this.table.push(entry);
            }
        }

        /**
         * Resizes the table to the new size.
         *
         * This will evict entries from the table until the total sum of iets entry's sizes is less
         * than or equal to the new size.
         *
         * This function does not check for conformance with the connection-specified maximum.
         *
         * @param newSize the new size of the dynamic table
         *
         * @see <a href="https://tools.ietf.org/html/rfc7540#section-4.3">RFC 7541 Section 4.3</a>
         */
        private void resize(final int newSize) {
            this.capacity = newSize;
            while (this.size >= newSize) {
                this.pop();
            }
        }

        /**
         * Removes the most recent entry from the table.
         */
        private void pop() {
            final var entry = this.table.pop();
            this.size -= entry.length;
        }

    }
}
