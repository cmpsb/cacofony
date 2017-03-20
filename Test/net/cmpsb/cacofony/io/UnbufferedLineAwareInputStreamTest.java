package net.cmpsb.cacofony.io;

import java.io.ByteArrayInputStream;

/**
 * Tests for the non-aware to line-aware input stream wrapper.
 *
 * @author Luc Everse
 */
public class UnbufferedLineAwareInputStreamTest
        extends LineAwareInputStreamTest<UnbufferedLineAwareInputStream> {

    public UnbufferedLineAwareInputStream getStream(final byte[] packet) {
        final ByteArrayInputStream source = new ByteArrayInputStream(packet);
        return new UnbufferedLineAwareInputStream(source);
    }
}
