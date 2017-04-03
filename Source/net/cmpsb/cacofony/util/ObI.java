package net.cmpsb.cacofony.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Collection creation utilities.
 *
 * @author Luc Everse
 */
public class ObI {
    /**
     * Creates a hash map based on its parameters.
     * <p>
     * There must be an even number of parameters, because otherwise there would be a key without
     * an assigned value.
     * <p>
     * Example:
     * <pre>
     * {@code
     *  final Map<String, Integer> = new ObI().map(
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
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> map(final Object... entries) {
        if ((entries.length & 1) != 0) {
            throw new IllegalArgumentException("Odd number of entries.");
        }

        final Map<K, V> map = new HashMap<>();

        for (int i = 0; i < entries.length; i += 2) {
            final K key = (K) entries[i];
            final V val = (V) entries[i + 1];
            map.put(key, val);
        }

        return map;
    }

    /**
     * Creates a set containing the entries in the parameters.
     *
     * @param entries the entries to create the set of
     * @param <T>     the type of the entries
     *
     * @return a set containing the entries
     */
    public <T> Set<T> set(final T... entries) {
        final Set<T> set = new HashSet<>();
        set.addAll(Arrays.asList(entries));
        return set;
    }
}
