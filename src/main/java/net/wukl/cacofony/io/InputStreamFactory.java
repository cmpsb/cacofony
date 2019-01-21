package net.wukl.cacofony.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Luc Everse
 */
@FunctionalInterface
public interface InputStreamFactory {
    /**
     * Creates an input stream that reads from another input stream.
     *
     * @param source the input stream to read from
     *
     * @return a new input stream that reads from {@code source}
     *
     * @throws IOException if an I/O error occurs
     */
    InputStream construct(InputStream source) throws IOException;
}
