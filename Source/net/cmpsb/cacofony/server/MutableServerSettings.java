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
     * Whether to add the Server header to each response.
     */
    private boolean broadcastServerVersion = true;

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
     * Sets whether the server may compress responses.
     *
     * @param enabled true to allow the server to compress responses, false otherwise
     */
    public void setCompressionEnabled(final boolean enabled) {
        this.compressionEnabled = enabled;
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
     * Sets whether the server may try to compress responses that don't care about compression.
     *
     * @param compress whether to compress any response that explicitly allows compression
     */
    public void setCompressByDefault(final boolean compress) {
        this.compressByDefault = compress;
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

    /**
     * Sets the list of compression algorithms the server may accept and send.
     *
     * @param algorithms the list of algorithms
     */
    public void setCompressionAlgorithms(final List<TransferEncoding> algorithms) {
        this.compressionAlgorithms = algorithms;
    }

    /**
     * Returns whether the server may add its name and version to each response.
     *
     * @return whether to add the Server header
     */
    @Override
    public boolean mayBroadcastServerVersion() {
        return this.broadcastServerVersion;
    }

    /**
     * Sets whether the server is allowed to add its own name and version to each response.
     *
     * @param broadcast true to allow the server to add a Server header, otherwise false
     */
    public void setBroadcastServerVersion(final boolean broadcast) {
        this.broadcastServerVersion = broadcast;
    }
}
