package net.cmpsb.cacofony.mime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A strictly validating parser for MIME types.
 * <p>
 * This parser will check for full rfc2045 compliance.
 *
 * @author Luc Everse
 */
public class StrictMimeParser implements MimeParser {
    /**
     * A regex matching valid MIME types.
     */
    private static final String MIME_REGEX =
            "(?<type>\\*|[a-zA-Z0-9][\\w.+!#$&^\\-]*)"
            + "\\s*/\\s*"
            + "(?<subtype>\\*|[a-zA-Z0-9][\\w.+!#$&^\\-]*)"
            + "(?<params>.*)";

    /**
     * A pattern matching valid MIME types.
     */
    private static final Pattern MIME_PATTERN = Pattern.compile(MIME_REGEX);

    /**
     * A regex matching MIME type parameters.
     */
    private static final String PARAMETER_REGEX =
            "(?:\\s*;\\s*"
            + "(?<key>[a-zA-Z0-9][\\w.+!#$&^\\-]*)"
            + "\\s*=\\s*"
            + "(?<value>[^;]*))";

    /**
     * A pattern matching MIME type parameters.
     */
    private static final Pattern PARAMETER_PATTERN = Pattern.compile(PARAMETER_REGEX);

    /**
     * Parses a string into a MIME type.
     *
     * @param plain the source string to parse
     *
     * @return a mime type
     */
    public MimeType parse(final String plain) {
        final Matcher mimeMatcher = MIME_PATTERN.matcher(plain);

        if (!mimeMatcher.matches()) {
            throw new InvalidMimeTypeException();
        }

        final String type = mimeMatcher.group("type");
        final String subType = mimeMatcher.group("subtype");
        final String params = mimeMatcher.group("params");

        final MimeType mimeType = new MimeType(type, subType);

        final Matcher paramMatcher = PARAMETER_PATTERN.matcher(params);
        while (paramMatcher.find()) {
            final String key = paramMatcher.group("key");
            final String value = paramMatcher.group("value").trim();

            mimeType.getParameters().put(key, value);
        }

        mimeType.recalculateRank();
        return mimeType;
    }
}
