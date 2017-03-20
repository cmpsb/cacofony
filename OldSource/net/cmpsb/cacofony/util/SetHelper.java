package net.cmpsb.cacofony.util;

import java.util.HashSet;
import java.util.Set;

/**
 * A utility function for creating sets.
 *
 * @author Luc Everse
 */
public final class SetHelper {
    /**
     * Do not instantiate.
     */
    private SetHelper() {
        throw new AssertionError("Do not instantiate.");
    }

    /**
     * Create a set of values.
     *
     * @param values the values to put in the set
     * @param <T>    the type of the values
     *
     * @return a set containing the values
     */
    public static <T> Set<T> asSet(final T... values) {
        final HashSet<T> set = new HashSet<>();

        for (final T value : values) {
            set.add(value);
        }

        return set;
    }
}
