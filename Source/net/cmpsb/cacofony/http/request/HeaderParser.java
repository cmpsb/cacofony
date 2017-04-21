package net.cmpsb.cacofony.http.request;

import net.cmpsb.cacofony.http.exception.BadRequestException;
import net.cmpsb.cacofony.io.LineAwareInputStream;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A context-agnostic parser for HTTP headers.
 *
 * @author Luc Everse
 */
public class HeaderParser {
    /**
     * Parses a set of headers from an input stream.
     *
     * @param in      the stream to read the headers from
     * @param request the request to write the headers to
     *
     * @throws IOException if an I/O error occurs while reading
     */
    public void parse(final LineAwareInputStream in, final MutableRequest request)
            throws IOException {
        while (true) {
            final String line = in.readLine();

            // Stop reading headers if the next line is empty.
            if (line.isEmpty()) {
                break;
            }

            // Find where the name ends and the value starts. Parse them.
            final int colonIdx = line.indexOf(':');

            // Error if there's no colon, indicating an obs-fold.
            if (colonIdx == -1) {
                throw new BadRequestException("Obs-folding is prohibited.");
            }

            final String key = line.substring(0, colonIdx).toLowerCase();
            final String value = line.substring(colonIdx + 1, line.length()).trim();

            // If the header key ends with a space, refuse to continue.
            if (key.endsWith(" ")) {
                throw new BadRequestException("Illegal space after header key.");
            }

            // Add it to the collection.
            request.getHeaders().computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
    }
}
