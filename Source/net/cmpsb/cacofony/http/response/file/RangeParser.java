package net.cmpsb.cacofony.http.response.file;

import net.cmpsb.cacofony.http.exception.BadRequestException;
import net.cmpsb.cacofony.http.exception.HttpException;
import net.cmpsb.cacofony.http.request.HeaderValueParser;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.http.response.ResponseCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A parser for HTTP Range headers.
 * <p>
 * This parser only understands byte ranges, other types are ignored.
 *
 * @author Luc Everse
 */
public class RangeParser {
    /**
     * The maximum amount of range segments the server is willing to process.
     */
    private static final int MAX_RANGES = 16;

    /**
     * The header value parser to use.
     */
    private final HeaderValueParser parser;

    /**
     * Creates a new range parser.
     *
     * @param parser the header value parser to use
     */
    public RangeParser(final HeaderValueParser parser) {
        this.parser = parser;
    }

    /**
     * Parses a request's ranges into a comprehensive set.
     *
     * @param request the request to parse
     * @param size    the total size of the response to range
     *
     * @return a list of byte ranges
     */
    public List<Range> parse(final Request request, final long size) {
        final List<Range> ranges = new ArrayList<>();

        final List<String> rValues = this.parser.parseCommaSeparated(request, "Range");

        if (rValues.size() > MAX_RANGES) {
            throw new BadRequestException("Too many ranges.");
        }

        int candidates = 0;
        String unit = "_";
        for (final String value : rValues) {
            final String rangeStr;

            // Scan for a possible new unit.
            final int equalsIndex = value.indexOf('=');
            if (equalsIndex != -1) {
                unit = value.substring(0, equalsIndex);
                rangeStr = value.substring(equalsIndex + 1);
            } else {
                rangeStr = value;
            }

            // Skip this range if the current unit is unknown.
            if (!unit.equals("bytes")) {
                continue;
            }

            final Range range = this.parseRange(rangeStr, size);
            if (range != null) {
                ranges.add(range);
            }

            ++candidates;
        }

        // Throw an error if all the ranges are invalid.
        if (candidates > 0 && ranges.isEmpty()) {
            final HttpException exception = new HttpException(ResponseCode.RANGE_NOT_SATISFIABLE);

            exception.setHeader("Content-Range", "bytes */" + size);

            throw exception;
        }

        ranges.removeIf(Objects::isNull);

        return ranges;
    }

    /**
     * Parses a string into a Range.
     *
     * @param rangeStr the string to parse
     * @param size     the total size of the resource to range
     *
     * @return a range matching that string
     */
    public Range parseRange(final String rangeStr, final long size) {
        final int hyphenIndex = rangeStr.indexOf('-');

        // If the hyphen is at the start, construct a range loading the last n bytes.
        if (hyphenIndex == 0) {
            final String lengthStr = rangeStr.substring(1);
            final long length = Long.parseLong(lengthStr);

            return this.construct(size - length, size - 1, size);
        }

        // Split the string.
        final String startStr = rangeStr.substring(0, hyphenIndex);
        final long start = Long.parseLong(startStr);

        final String endStr = rangeStr.substring(hyphenIndex + 1);

        // If the hyphen is at the end, offset the range.
        if (endStr.isEmpty()) {
            return this.construct(start, size - 1, size);
        }

        // Otherwise copy the completely specified range.
        final long end = Long.parseLong(endStr);
        return this.construct(start, end, size);
    }

    /**
     * Creates a new range or returns {@code null} if the range is invalid.
     *
     * @param start the start of the range
     * @param end   the end of the range
     * @param size  the size of the file to send
     *
     * @return a new range or {@code null}
     */
    private Range construct(final long start, final long end, final long size) {
        if (start < 0 || end < 0 || start > size - 1 || end < start) {
            return null;
        }

        return new Range(start, end);
    }
}
