package net.cmpsb.cacofony.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Enumeration helper classes.
 *
 * @author Luc Everse
 */
public final class EnumerationHelpers {
    /**
     * Do not instantiate.
     */
    private EnumerationHelpers() {
        throw new AssertionError("Do not instantiate.");
    }

    /**
     * Wrap a collection into a one-use enumeration.
     * The enumeration will take an iterator from the given collection.
     *
     * @param collection the collection to wrap
     * @param <T>        the type the collection contains
     *
     * @return a single-use enumeration based on the given collection
     */
    public static <T> Enumeration<T> wrap(final Collection<T> collection) {
        return new Enumeration<T>() {
            private final Iterator<T> iterator = collection.iterator();

            @Override
            public boolean hasMoreElements() {
                return this.iterator.hasNext();
            }

            @Override
            public T nextElement() {
                return this.iterator.next();
            }
        };
    }
}
