package net.wukl.cacofony.http.request;

import net.wukl.cacofony.http.exception.BadRequestException;
import net.wukl.cacofony.io.LineAwareInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A context-agnostic parser for HTTP headers.
 *
 * @author Luc Everse
 */
public class HeaderParser {
    /**
     * Parses a set of headers from an input stream.
     *
     * @param in the stream to read the headers from
     *
     * @return the parsed headers
     *
     * @throws IOException if an I/O error occurs while reading
     */
    public Map<String, List<String>> parse(final LineAwareInputStream in)
            throws IOException {
        final Map<String, List<String>> headers = new HashMap<>();

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
            headers.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }

        return headers;
    }
}
