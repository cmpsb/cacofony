package net.cmpsb.cacofony.server;

import net.cmpsb.cacofony.http.encoding.TransferEncoding;
import net.cmpsb.cacofony.util.Ob;

import java.util.HashSet;
import java.util.Set;

/**
 * Simply returns all default setting values.
 *
 * @author Luc Everse
 */
public class DefaultSettings implements ServerSettings {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompressionEnabled() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canCompressByDefault() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<TransferEncoding> getCompressionAlgorithms() {
        return Ob.set(
                TransferEncoding.GZIP,
                TransferEncoding.DEFLATE
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mayBroadcastServerVersion() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Port> getPorts() {
        return new HashSet<>();
    }
}
