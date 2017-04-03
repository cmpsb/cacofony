package net.cmpsb.cacofony.server;

import net.cmpsb.cacofony.http.encoding.TransferEncoding;

import java.util.Arrays;
import java.util.List;

/**
 * A class containing all server settings.
 *
 * @author Luc Everse
 */
public class MutableServerSettings implements ServerSettings {
    /**
     * Whether to allow compression at all.
     */
    private boolean compressionEnabled = true;

    /**
     * Whether to compress responses that don't care about compression.
     */
    private boolean compressByDefault = false;

    /**
     * A list of enabled compressing transfer encodings.
     */
    private List<TransferEncoding> compressionAlgorithms = Arrays.asList(
            TransferEncoding.GZIP,
            TransferEncoding.DEFLATE
    );

    /**
     * Returns whether the server is allowed to try to apply compression if the client supports it.
     *
     * @return whether the server is allowed to try to apply compression
     */
    @Override
    public boolean isCompressionEnabled() {
        return this.compressionEnabled;
    }

    /**
     * Returns whether the server is allowed to try to apply compression to responses that don't
     * explicitly allow it.
     *
     * @return whether to try to compress responses that don't care about compression
     */
    @Override
    public boolean canCompressByDefault() {
        return this.compressByDefault;
    }

    /**
     * Returns a list of allowed compression algorithms.
     *
     * @return a list of allowed compression algorithms
     */
    @Override
    public List<TransferEncoding> getCompressionAlgorithms() {
        return this.compressionAlgorithms;
    }
}
