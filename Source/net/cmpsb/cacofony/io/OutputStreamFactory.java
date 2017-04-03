package net.cmpsb.cacofony.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Luc Everse
 */
@FunctionalInterface
public interface OutputStreamFactory {
    /**
     * Creates an output stream that writes to another output stream.
     *
     * @param target the output stream to write to
     *
     * @return a new output stream that writes to {@code target}
     *
     * @throws IOException if an I/O error occurs
     */
    OutputStream construct(OutputStream target) throws IOException;
}
