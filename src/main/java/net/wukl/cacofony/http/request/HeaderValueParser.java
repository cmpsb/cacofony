package net.wukl.cacofony.http.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A parser for common header value formats.
 *
 * @author Luc Everse
 */
public class HeaderValueParser {
    /**
     * A pattern that splits by comma.
     */
    private static final Pattern COMMA_SPLITTER = Pattern.compile("\\s*,\\s*");

    /**
     * A pattern that splits by semicolon.
     */
    private static final Pattern SEMICOLON_SPLITTER = Pattern.compile("\\s*;\\s*");

    /**
     * A regex that extracts a weight from a string.
     */
    private static final String WEIGHT_REGEX = "q\\s*=\\s*(?<WEIGHT>[0-9]+(?:\\.[0-9]+)?)";

    /**
     * A pattern that extracts a weight from a string.
     */
    private static final Pattern WEIGHT_PATTERN = Pattern.compile(WEIGHT_REGEX);

    /**
     * Parsers a header that contains comma-separated values.
     * <p>
     * Multiple header entries for the same name are combined into one list.
     *
     * @param request the request the header is in
     * @param header  the name of the header to parse
     *
     * @return all values in that header
     */
    public List<String> parseCommaSeparated(final Request request, final String header) {
        if (!request.hasHeader(header)) {
            return Collections.emptyList();
        }

        final List<String> values = new ArrayList<>();

        final List<String> headers = request.getHeaders(header);
        for (final String value : headers) {
            values.addAll(Arrays.asList(COMMA_SPLITTER.split(value)));
        }

        return values;
    }

    /**
     * Parses a header that contains comma-separated values that may have a quality weight.
     * <p>
     * Multiple header entries for the same name are combined into one list.
     * <p>
     * The quality weight gets stripped from the returned values after sorting them.
     *
     * @param request the request the header is in
     * @param header  the name of the header
     *
     * @return all values in that header, sorted by preference
     */
    public List<String> parseWeightedValues(final Request request, final String header) {
        return this.parseCommaSeparated(request, header)
                .stream()
                .map(this::giveWeight)
                .sorted()
                .map(v -> v.value)
                .collect(Collectors.toList());
    }

    /**
     * Associates a weight to a value.
     *
     * @param rawValue the value to parse and weigh
     *
     * @return a weighted value
     */
    private WeightedValue<String> giveWeight(final String rawValue) {
        final String[] parts = SEMICOLON_SPLITTER.split(rawValue, 1);
        final String value = parts[0];
        final String tail = parts[1];

        double weight;
        final Matcher qualityMatcher = WEIGHT_PATTERN.matcher(tail);
        if (qualityMatcher.find()) {
            try {
                weight = Double.parseDouble(qualityMatcher.group("WEIGHT"));
            } catch (final NumberFormatException ex) {
                weight = 1;
            }
        } else {
            weight = 0;
        }

        return new WeightedValue<>(value, weight);
    }

    /**
     * A container class for weighted values.
     *
     * @param <T> the type the container holds
     */
    private class WeightedValue<T> implements Comparable<WeightedValue<T>> {
        /**
         * The actual value.
         */
        private final T value;

        /**
         * The value's weight.
         */
        private final double weight;

        /**
         * Creates a new weighted value.
         *
         * @param value  the value
         * @param weight the weight
         */
        WeightedValue(final T value, final double weight) {
            this.value = value;
            this.weight = weight;
        }

        /**
         * Compares this object with the specified object for order.  Returns a
         * negative integer, zero, or a positive integer as this object is less
         * than, equal to, or greater than the specified object.
         *
         * @param o the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object
         * is less than, equal to, or greater than the specified object.
         * @throws NullPointerException if the specified object is null
         * @throws ClassCastException   if the specified object's type prevents it
         *                              from being compared to this object.
         */
        @Override
        public int compareTo(final WeightedValue<T> o) {
            return (int) (this.weight - o.weight);
        }
    }
}
