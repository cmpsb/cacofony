package net.wukl.cacofony.mime;

/**
 * A parser for MIME types.
 *
 * @author Luc Everse
 */
public interface MimeParser {
    /**
     * Parses a string into a MIME type.
     *
     * @param plain the source string to parse
     *
     * @return a mime type
     */
    MimeType parse(String plain);
}
