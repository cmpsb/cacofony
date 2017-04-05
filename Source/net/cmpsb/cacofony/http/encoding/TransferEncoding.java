package net.cmpsb.cacofony.http.encoding;

import net.cmpsb.cacofony.io.InputStreamFactory;
import net.cmpsb.cacofony.io.OutputStreamFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * A HTTP transfer encoding.
 *
 * @author Luc Everse
 */
public enum TransferEncoding {
    /**
     * The response is sent in chunks.
     */
    CHUNKED("chunked", null, null, false),

    /**
     * The response is gzipped.
     */
    GZIP("gzip", target -> new GZIPOutputStream(target, true), GZIPInputStream::new, true),

    /**
     * The response is zipped.
     */
    DEFLATE("deflate", DeflaterOutputStream::new, InflaterInputStream::new, true),

    /**
     * Any compression goes. Defaults to gzip.
     */
    ANY_COMPRESSOR("*", target -> new GZIPOutputStream(target, true), GZIPInputStream::new, true);

    /**
     * A static mapping of all known encodings and their names.
     */
    private static final Map<String, TransferEncoding> BY_NAME_MAP = new HashMap<>();

    static {
        BY_NAME_MAP.put("chunked",   CHUNKED);
        BY_NAME_MAP.put("x-chunked", CHUNKED);

        BY_NAME_MAP.put("gzip",   GZIP);
        BY_NAME_MAP.put("x-gzip", GZIP);

        BY_NAME_MAP.put("deflate",   DEFLATE);
        BY_NAME_MAP.put("x-deflate", DEFLATE);

        BY_NAME_MAP.put("*", GZIP);
    }

    /**
     * Looks up a transfer encoding by its HTTP name.
     *
     * @param name the name
     *
     * @return a transfer encoding or {@code null} if there is no encoding with that name
     */
    public static TransferEncoding get(final String name) {
        return BY_NAME_MAP.get(name);
    }

    /**
     * The canonical name.
     */
    private final String httpName;

    /**
     * A factory for output streams producing this encoding.
     */
    private final OutputStreamFactory outputStreamFactory;

    /**
     * A factory for input streams comprehending this encoding.
     */
    private final InputStreamFactory inputStreamFactory;

    /**
     * Whether the encoding is a compressor or not.
     */
    private final boolean isCompressor;

    /**
     * Creates a new transfer encoding.
     *
     * @param httpName            the canonical HTTP name for this encoding
     * @param outputStreamFactory a factory for output streams producing this encoding
     * @param inputStreamFactory  a factory for input streams producing this encoding
     * @param isCompressor        whether the encoding is a compressor or not
     */
    TransferEncoding(final String httpName,
                     final OutputStreamFactory outputStreamFactory,
                     final InputStreamFactory  inputStreamFactory,
                     final boolean isCompressor) {
        this.httpName = httpName;
        this.outputStreamFactory = outputStreamFactory;
        this.inputStreamFactory  = inputStreamFactory;
        this.isCompressor = isCompressor;
    }

    /**
     * Returns the canonical HTTP name for this encoding.
     *
     * @return the canonical HTTP name
     */
    public String getHttpName() {
        return this.httpName;
    }

    /**
     * Constructs an output stream for this transfer encoding.
     *
     * @param target the target stream this encoder will write to
     *
     * @return an output stream for this transfer encoding
     *
     * @throws IOException if an I/O error occurs
     */
    public OutputStream construct(final OutputStream target) throws IOException {
        return this.outputStreamFactory.construct(target);
    }

    /**
     * Constructs an input stream for this transfer encoding.
     *
     * @param source the source stream this encoder will read from
     *
     * @return an input stream for this transfer encoding
     *
     * @throws IOException if an I/O error occurs
     */
    public InputStream construct(final InputStream source) throws IOException {
        return this.inputStreamFactory.construct(source);
    }

    /**
     * Returns whether the encoding is a compressing encoding.
     *
     * @return true if the encoding is a compressing encoding
     */
    public boolean isCompressor() {
        return this.isCompressor;
    }
}
