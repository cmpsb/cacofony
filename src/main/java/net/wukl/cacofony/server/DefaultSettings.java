package net.wukl.cacofony.server;

import net.wukl.cacofony.http.encoding.TransferEncoding;
import net.wukl.cacofony.util.Ob;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxConcurrentStreams() {
        return 128;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHttp2Enabled() {
        return false;
    }
}
