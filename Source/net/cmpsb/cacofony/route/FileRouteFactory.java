package net.cmpsb.cacofony.route;

import net.cmpsb.cacofony.http.request.HeaderValueParser;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.http.response.file.CachableResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * @author Luc Everse
 */
public abstract class FileRouteFactory {
    private static final Logger logger = LoggerFactory.getLogger(FileRouteFactory.class);

    /**
     * The header value parser to use.
     */
    private final HeaderValueParser valueParser;

    /**
     * Creates a new file route factory.
     *
     * @param valueParser the value parser to use
     */
    public FileRouteFactory(final HeaderValueParser valueParser) {
        this.valueParser = valueParser;
    }

    /**
     * Returns whether the controller may return a HTTP 304.
     *
     * @param request  the request
     * @param response the response that may be a 304
     *
     * @return true if the response can be an HTTP 304, otherwise false
     */
    public boolean can304(final Request request, final CachableResponse response) {
        // Check the If-None-Match header.
        final List<String> etags = this.valueParser.parseCommaSeparated(request, "If-None-Match");

        if (etags.contains(response.getEtag())) {
            return true;
        }

        // Otherwise try the If-Modified-Since header.
        final ZonedDateTime modifiedSince = this.tryParseModifiedSince(request);
        if (modifiedSince != null) {
            // java.time is hardcore.
            final Instant modificationInstant = Instant.ofEpochMilli(response.getLastModified());
            final ZonedDateTime modificationDate =
                    ZonedDateTime.ofInstant(modificationInstant, ZoneOffset.UTC);

            if (modifiedSince.isAfter(modificationDate)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Tries to parse a date from the If-Modified-Since header.
     *
     * @param request the request the header is in
     *
     * @return a datetime or {@code null}
     */
    private ZonedDateTime tryParseModifiedSince(final Request request) {
        final String modifiedSince = request.getHeader("If-Modified-Since");

        if (modifiedSince == null) {
            return null;
        }

        ZonedDateTime dateTime;
        try {
            dateTime = ZonedDateTime.parse(modifiedSince, DateTimeFormatter.RFC_1123_DATE_TIME);
            return dateTime;
        } catch (final DateTimeParseException ex) {
            // Pass, try the next format.
        }

        // Or not.
        logger.warn("Unparsable If-Modified-Since: {}.", modifiedSince);
        return null;
    }
}
