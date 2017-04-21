package net.cmpsb.cacofony.util;

import java.util.Map;
import java.util.Set;

/**
 * @author Luc Everse
 */
public final class Ob {
    /**
     * The private instance for the static interface.
     */
    private static final ObI INSTANCE = new ObI();

    /**
     * Do not instantiate.
     */
    private Ob() {
        throw new AssertionError("Do not instantiate.");
    }

    /**
     * Creates a hash map based on its parameters.
     * <p>
     * There must be an even number of parameters, because otherwise there would be a key without
     * an assigned value.
     * <p>
     * Example:
     * <pre>
     * {@code
     *  final Map<String, Integer> = Ob.map(
     *      "true", 0,
     *      "false", 1,
     *      "file not found", 2
     *  );
     * }
     * </pre>
     * This is fully equivalent to
     * <pre>
     * {@code
     *  final Map<String, Integer> map = new HashMap<>();
     *  map.put("true", 0);
     *  map.put("false", 1);
     *  map.put("file not found", 2);
     * }
     * </pre>
     * With the exception that this method can be used in-line.
     *
     * @param entries  the entries in the map
     * @param <K>      the type of the keys
     * @param <V>      the type of the values
     *
     * @return a hash map containing the entries
     *
     * @throws ClassCastException if one of the entries is of the wrong type
     * @throws IllegalArgumentException if there is an odd number of entries
     * @throws NullPointerException if at least one of the types is {@code null}
     */
    public static <K, V> Map<K, V> map(final Object... entries) {
        return INSTANCE.map(entries);
    }

    /**
     * Creates a set containing the entries in the parameters.
     *
     * @param entries the entries to create the set of
     * @param <T>     the type of the entries
     *
     * @return a set containing the entries
     */
    public static <T> Set<T> set(final T... entries) {
        return INSTANCE.set(entries);
    }

    /**
     * Checks whether each pair of arguments is equal.
     * <p>
     * Calling this function like this:
     * <pre>
     * {@code
     *  Ob.multiEquals(
     *      one, other,
     *      etaoin, shrdlu
     *  );
     * }
     * </pre>
     * is equivalent to:
     * <pre>
     * {@code
     *  (Object.equals(one, other)
     *  && Object.equals(etaoin, shrdlu));
     * }
     * </pre>
     * <p>
     * This operation is vacuously true; it will return {@code true} if called without values.
     *
     * @param values the values to check for equality
     *
     * @return {@code true} if all pairs are equal, otherwise {@code false}
     *
     * @throws IllegalArgumentException if the number of values is odd
     */
    public static boolean multiEquals(final Object... values) {
        return INSTANCE.multiEquals(values);
    }
}
