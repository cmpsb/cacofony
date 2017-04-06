package net.cmpsb.cacofony.mime;

/**
 * A quick mime parser.
 * <p>
 * This parser will cut some corners, but is faster than the {@link StrictMimeParser}. It will
 * parse any valid MIME type, but wont error as quickly.
 *
 * @author Luc Everse
 */
public class FastMimeParser implements MimeParser {
    /**
     * Parses a string into a MIME type.
     *
     * @param plain the source string to parse
     * @return a mime type
     */
    @Override
    public MimeType parse(final String plain) {
        final int firstSlash = plain.indexOf('/');
        final int firstSemicolon = plain.indexOf(';');

        if (firstSlash == -1) {
            throw new InvalidMimeTypeException(plain);
        }

        final String mainType = plain.substring(0, firstSlash);

        // Stop parsing if there are no parameters.
        if (firstSemicolon == -1) {
            final String subType = plain.substring(firstSlash + 1);
            return new MimeType(mainType, subType);
        }

        // Otherwise start processing those.
        final String subType = plain.substring(firstSlash + 1, firstSemicolon);

        final MimeType type = new MimeType(mainType, subType);

        int lastSemicolon = firstSemicolon;
        while (lastSemicolon != -1) {
            // Locate the key between the last semicolon and the next equals sign.
            final int nextEquals = plain.indexOf('=', lastSemicolon + 1);

            if (nextEquals == -1) {
                throw new InvalidMimeTypeException(
                        "No equals sign in parameter for type \"" + plain + "\"."
                );
            }

            final String key = plain.substring(lastSemicolon + 1, nextEquals).trim();

            if (key.isEmpty()) {
                throw new InvalidMimeTypeException("Empty key in type \"" + plain + "\".");
            }

            // Then find the value between this equals sign and the next semicolon.
            lastSemicolon = plain.indexOf(';', nextEquals + 1);

            final String value;
            if (lastSemicolon == -1) {
                value = plain.substring(nextEquals + 1).trim();
            } else {
                value = plain.substring(nextEquals + 1, lastSemicolon).trim();
            }

            type.getParameters().put(key, value);
        }


        return type;
    }
}
