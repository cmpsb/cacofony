package net.cmpsb.cacofony.http.response.file;

import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.http.response.Response;
import net.cmpsb.cacofony.http.response.ResponseCode;
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
import java.util.Collections;
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
    private List<Range> ranges = Collections.emptyList();

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
        // It's too large as a decimal value, so binary I guess?
        final long basis = 0b1100101111110010100111001110010010000100001000100010001100100101L;
        final long prime = 1099511628211L;

        long hash = basis;
        for (int i = 0; i < Long.BYTES; ++i) {
            final long b = (this.lastModified >> (i * 8)) & 0xFF;
            hash ^= b;
            hash *= prime;
        }

        return Long.toString(Math.abs(hash), 32);
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
     * Sets the ranges the response should send.
     *
     * @param ranges the ranges
     */
    public void setRanges(final List<Range> ranges) {
        this.ranges = ranges;
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

        if (this.ranges.size() == 1) {
            final Range range = this.ranges.get(0);
            this.setHeader("Content-Range", "bytes " + range + "/" + this.size);
        } else if (this.ranges.size() > 1) {
            this.boundary = this.generateBoundary();

            final MimeType multipartType = new MimeType("multipart", "byteranges");
            multipartType.getParameters().put("boundary", this.boundary);
            super.setContentType(multipartType);

            this.setStatus(ResponseCode.PARTIAL_CONTENT);
        }

        super.prepare(request);
    }

    /**
     * Generates a random boundary string.
     *
     * @return a boundary string
     */
    private String generateBoundary() {
        return new Random()
                .longs(4)
                .map(Math::abs)
                .mapToObj(n -> Long.toString(n, 32))
                .collect(Collectors.joining());
    }

    /**
     * Writes the response body to the output stream.
     *
     * @param out the client's output stream
     * @throws IOException if an I/O error occurs during writing
     */
    @Override
    public void write(final OutputStream out) throws IOException {
        // If this request is not ranged, reply with the whole file.
        if (this.ranges.isEmpty()) {
            this.writeFull(out);
        } else if (this.ranges.size() == 1) {
            this.writeSingleRange(out);
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
     * Writes the only range if the response is single-ranged.
     *
     * @param out the client's output stream
     *
     * @throws IOException if an I/O error occurs
     */
    private void writeSingleRange(final OutputStream out) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(this.file, "r")) {
            final byte[] buffer = new byte[this.bufferSize];
            final Range range = this.ranges.get(0);

            raf.seek(range.getStart());

            int bytesRead;
            long length = range.getLength();
            while (length > 0) {
                final int toRead = Math.min((int) length, this.bufferSize);
                bytesRead = raf.read(buffer, 0, toRead);

                if (bytesRead == -1) {
                    throw new IOException("EOF before end of range.");
                }

                out.write(buffer, 0, bytesRead);

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
                        + range
                        + '/'
                        + commonHeaderEnd;

                out.write(header.getBytes(StandardCharsets.ISO_8859_1));

                raf.seek(range.getStart());

                long length = range.getLength();
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
}
