package net.cmpsb.cacofony.http.response;

import net.cmpsb.cacofony.http.exception.BadRequestException;
import net.cmpsb.cacofony.http.request.HeaderValueParser;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.mime.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * A response that serves a file by reading it from disk.
 * <p>
 * An ETag value is included with each file sent. This tag is based on the 64-bit FNV-1a hash
 * of the file's modification date.
 * <p>
 * The response type also supports ranged requests, which causes the system to only respond with
 * fragments instead of the full file. No extra controller support is necessary.
 *
 * @author Luc Everse
 */
public class FileResponse extends Response {
    private static final Logger logger = LoggerFactory.getLogger(FileResponse.class);

    /**
     * The maximum amount of range segments the server is willing to process.
     */
    private static final int MAX_RANGES = 16;

    /**
     * The internal buffer used to transfer bytes to the client.
     */
    private int bufferSize = 8192;

    /**
     * The file's size in bytes.
     */
    private long size = 0;

    /**
     * The date the file was last modified.
     */
    private long lastModified = Long.MAX_VALUE;

    /**
     * The stream to read the file from.
     */
    private File file;

    /**
     * The ranges to send. If empty, then the whole file is sent.
     */
    private final List<Range> ranges = new LinkedList<>();

    /**
     * The boundary string between ranges.
     */
    private String boundary;

    /**
     * The file's content type.
     * This is a separate variable because ranged requests require a response content type of
     * {@code multipart/byteranges}.
     */
    private MimeType actualContentType;

    /**
     * The file's ETag.
     */
    private final String etag;

    /**
     * Creates a new response sending a file.
     *
     * @param file the file to send
     *
     * @throws FileNotFoundException if the file does not exist
     */
    public FileResponse(final File file) throws FileNotFoundException {
        this.file = file;
        this.size = file.length();
        this.lastModified = file.lastModified();

        this.etag = this.generateEtag();
    }

    /**
     * Sets the content type, while keeping a copy of it locally in case the request is a ranged
     * one.
     *
     * @param type the content type
     */
    @Override
    public void setContentType(final MimeType type) {
        super.setContentType(type);

        this.actualContentType = type;
    }

    /**
     * Calculates and returns the response's ETag.
     * <p>
     * The ETag is based on the SHA-256 of the file's modification date.
     *
     * @return the etag
     */
    public String getEtag() {
        return this.etag;
    }

    /**
     * Generates an ETag based on the file's modification date.
     *
     * @return an ETag
     */
    private String generateEtag() {
        final long basis = 0b1100101111110010100111001110010010000100001000100010001100100101L;
        final long prime = 1099511628211L;

        long hash = basis;
        for (int i = 0; i < Long.BYTES; ++i) {
            final long b = (this.lastModified >> (i * 8)) & 0xFF;
            hash ^= b;
            hash *= prime;
        }

        return Long.toString(hash, 32);
    }

    /**
     * Returns the datetime the file was last modified.
     *
     * @return the last modification date
     */
    public long getLastModified() {
        return this.lastModified;
    }

    /**
     * Sets the size, in bytes, of the internal buffer used to transfer bytes from the file to the
     * client's output stream.
     *
     * @param bufferSize the size of the buffer, in bytes
     *
     * @throws IllegalArgumentException if the buffer size is non-positive
     */
    public void setBufferSize(final int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Non-positive buffer size.");
        }

        this.bufferSize = bufferSize;
    }

    /**
     * Prepares the response for transfer.
     *
     * @param request the request that triggered this response
     */
    @Override
    public void prepare(final Request request) {
        // Calculate the ETag based on the modification date.
        if (this.lastModified != Long.MAX_VALUE) {
            this.setHeader("ETag", this.etag);
        }

        this.buildRanges(request);

        if (!this.ranges.isEmpty()) {
            final MimeType multipartType = new MimeType("multipart", "byteranges");
            this.boundary = new Random()
                    .longs(4)
                    .map(Math::abs)
                    .mapToObj(n -> Long.toString(n, 32))
                    .collect(Collectors.joining());
            multipartType.getParameters().put("boundary", this.boundary);
            super.setContentType(multipartType);
            this.setStatus(ResponseCode.PARTIAL_CONTENT);
        }

        super.prepare(request);
    }

    /**
     * Parses a request's ranges into a comprehensive set.
     *
     * @param request the request to parse
     */
    private void buildRanges(final Request request) {
        // Don't build a range if the request doesn't allow it.
        if (!request.hasHeader("Range")
        || (request.hasHeader("If-Range") && !request.getHeader("If-Range").equals(this.etag))) {
            return;
        }

        final HeaderValueParser parser = new HeaderValueParser();
        final List<String> rValues = parser.parseCommaSeparated(request, "Range");

        if (rValues.size() > MAX_RANGES) {
            throw new BadRequestException("Too many ranges.");
        }

        String unit = "_";
        for (final String value : rValues) {
            final String rangeStr;

            // Scan for a possible new unit.
            final int equalsIndex = value.indexOf('=');
            if (equalsIndex != -1) {
                unit = value.substring(0, equalsIndex);
                rangeStr = value.substring(equalsIndex + 1);
            } else {
                rangeStr = value;
            }

            // Skip this range if the current unit is unknown.
            if (!unit.equals("bytes")) {
                continue;
            }

            this.ranges.add(this.parseRange(rangeStr));
        }

        logger.debug("HOLD ONTO YER HATS, RANGES BE INCOMING");
        this.ranges.forEach(r -> logger.debug("{}", r));
    }

    /**
     * Parses a string into a Range.
     *
     * @param rangeStr the string to parse
     *
     * @return a range matching that string
     */
    private Range parseRange(final String rangeStr) {
        final int hyphenIndex = rangeStr.indexOf('-');

        // If the hyphen is at the start, construct a range loading the last n bytes.
        if (hyphenIndex == 0) {
            final String lengthStr = rangeStr.substring(1);
            final long length = Long.parseLong(lengthStr);

            return new Range(this.size - length, this.size - 1);
        }

        // Split the string.
        final String startStr = rangeStr.substring(0, hyphenIndex);
        final long start = Long.parseLong(startStr);

        final String endStr = rangeStr.substring(hyphenIndex + 1);

        // If the hyphen is at the end, offset the range.
        if (endStr.isEmpty()) {
            return new Range(start, this.size - 1);
        }

        // Otherwise copy the completely specified range.
        final long end = Long.parseLong(endStr);
        return new Range(start, end);
    }

    /**
     * Writes the response body to the output stream.
     *
     * @param out the client's output stream
     * @throws IOException if an I/O error occurs during writing
     */
    @Override
    public void write(final OutputStream out) throws IOException {

        final RandomAccessFile raf = new RandomAccessFile(this.file, "r");

        // If this request is not ranged, reply with the whole file.
        if (this.ranges.isEmpty()) {
            this.writeFull(out);
        } else {
            this.writeRanged(out);
        }
    }

    /**
     * Writes the full file to the client.
     *
     * @param out the client's output stream
     *
     * @throws IOException if an I/O error occurs
     */
    private void writeFull(final OutputStream out) throws IOException {
        try (InputStream in = new FileInputStream(this.file)) {
            final byte[] buffer = new byte[this.bufferSize];

            int bytesRead;
            long length = this.size;
            while (length > 0) {
                bytesRead = in.read(buffer);

                if (bytesRead == -1) {
                    break;
                }

                out.write(buffer, 0, bytesRead);
                out.flush();

                length -= bytesRead;
            }
        }
    }

    /**
     * Writes some ranges of bytes from the file to the client.
     *
     * @param out the client's output stream
     *
     * @throws IOException if an I/O error occurs
     */
    private void writeRanged(final OutputStream out) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(this.file, "r")) {
            final byte[] buffer = new byte[this.bufferSize];

            // Build the parts of the string that occur many times and don't change between ranges.
            final String commonHeaderStart = "--"
                    + this.boundary
                    + "\r\nContent-Type: "
                    + this.actualContentType
                    + "\r\nContent-Range: bytes ";

            final String commonHeaderEnd = this.size + "\r\n\r\n";

            for (final Range range : this.ranges) {
                final String header = commonHeaderStart
                        + range.start
                        + '-'
                        + range.end
                        + '/'
                        + commonHeaderEnd;

                out.write(header.getBytes(StandardCharsets.ISO_8859_1));

                raf.seek(range.start);

                long length = range.end - range.start + 1;

                while (length > 0) {
                    final int toRead = Math.min((int) length, this.bufferSize);
                    int bytesRead = raf.read(buffer, 0, toRead);

                    if (bytesRead == -1) {
                        throw new IOException("EOF before end of range.");
                    }

                    out.write(buffer, 0, bytesRead);

                    length -= bytesRead;
                }
            }

            out.write("--".getBytes(StandardCharsets.ISO_8859_1));
            out.write(this.boundary.getBytes(StandardCharsets.ISO_8859_1));
            out.write("--".getBytes(StandardCharsets.ISO_8859_1));
            out.flush();
        }
    }

    /**
     * Calculates the length, in bytes, of the data to send.
     * <p>
     * If {@code -1}, then a collection of transfer encodings are applied. This allows for
     * big responses that don't fit entirely within memory.
     *
     * @return the length, in bytes, of the data to send or {@code -1} if it's unknown
     */
    @Override
    public long getContentLength() {
        if (this.ranges.isEmpty()) {
            return this.size;
        }

        return this.ranges.stream().mapToLong(Range::getLength).sum();
    }

    /**
     * A range in a Range header.
     */
    private class Range {
        /**
         * The first byte of the range, inclusive.
         */
        private long start;

        /**
         * The last byte of the range, inclusive.
         */
        private long end;

        /**
         * Creates a new inclusive range.
         *
         * @param start the first byte of the range
         * @param end   the last byte of the range
         */
        Range(final long start, final long end) {
            this.start = start;
            this.end = end;
        }

        /**
         * Calculates the length of the range in bytes.
         *
         * @return the length of the range in bytes
         */
        public long getLength() {
            return this.end - this.start + 1;
        }

        /**
         * Generates a string representing this range.
         *
         * @return a string representing this range
         */
        @Override
        public String toString() {
            return this.start + "-" + this.end;
        }
    }
}
