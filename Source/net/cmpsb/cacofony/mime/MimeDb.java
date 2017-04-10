package net.cmpsb.cacofony.mime;

import java.util.HashMap;
import java.util.Map;

/**
 * A database that maps file extensions to MIME types.
 *
 * @author Luc Everse
 */
public class MimeDb {
    /**
     * The mapping.
     */
    private final Map<String, MimeType> db = new HashMap<>();

    /**
     * Guesses the MIME type for a file extension.
     *
     * @param path the path to examine
     *
     * @return a fitting MIME type or {@code application/octet-stream}
     */
    public MimeType getForName(final String path) {
        // Find the last path separator in the path.
        final int slashIndex = path.lastIndexOf('/');
        final int backslashIndex = path.lastIndexOf('\\');

        final int pathSeparatorIndex = Math.max(slashIndex, backslashIndex);

        // Find the last period in the path.
        final int extensionIndex = path.lastIndexOf('.');

        // If there is no period or the period comes before the last path separator, there is no
        // valid extension, so return application/octet-stream.
        if (extensionIndex <= pathSeparatorIndex) {
            return MimeType.octetStream();
        }

        // Otherwise look it up in the mapping, returning application/octet-stream if the extension
        // is unknown.
        final String extension = path.substring(extensionIndex + 1);

        final MimeType type = this.db.get(extension);

        if (type == null) {
            return MimeType.octetStream();
        }

        return new MimeType(type);
    }

    /**
     * Registers a new MIME type for an extension.
     *
     * @param extension the extension
     * @param type      the MIME type
     */
    public void register(final String extension, final MimeType type) {
        this.db.put(extension, type);
    }
}
