package net.cmpsb.cacofony.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 *
 *
 * @author Luc Everse
 */
public final class ListHelpers {

    /**
     * Do not instantiate.
     */
    private ListHelpers() {
        throw new AssertionError("Do not instantiate.");
    }

    /**
     * Convert an enumeration into a list.
     *
     * @param enumeration the enumeration to convert
     * @param <T>         the element's type
     *
     * @return a list containing the elements originally in the enumeration
     */
    public static <T> List<T> fromEnumeration(final Enumeration<T> enumeration) {
        List<T> list = new ArrayList<>();

        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }

        return list;
    }
}
