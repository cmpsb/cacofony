package net.cmpsb.cacofony.server;

import net.cmpsb.cacofony.http.encoding.TransferEncoding;

import java.util.List;
import java.util.Set;

/**
 * @author Luc Everse
 */
public interface ServerSettings {
    /**
     * Returns whether the server is allowed to try to apply compression if the client supports it.
     * @return whether the server is allowed to try to apply compression
     */
    boolean isCompressionEnabled();

    /**
     * Returns whether the server is allowed to try to apply compression to responses that don't
     * explicitly allow it.
     *
     * @return whether to try to compress responses that don't care about compression
     */
    boolean canCompressByDefault();

    /**
     * Returns a list of allowed compression algorithms.
     * @return a list of allowed compression algorithms
     */
    List<TransferEncoding> getCompressionAlgorithms();

    /**
     * Returns whether the server may add its name and version to each response.
     * @return whether to add the Server header
     */
    boolean mayBroadcastServerVersion();

    /**
     * Returns the ports the server should listen on.
     *
     * @return the ports the server should listen on
     */
    Set<Port> getPorts();
}
