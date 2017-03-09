package net.cmpsb.cacofony.routing;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * An entry in the routing table.
 *
 * @author Luc Everse
 */
public class RoutingEntry implements Comparable<RoutingEntry> {
    /**
     * The routing entry's programmer-friendly name.
     */
    private final String name;

    /**
     * The routing path the entry represents.
     */
    private final String path;

    /**
     * The compiled regex pattern derived from the path.
     */
    private final Pattern pattern;

    /**
     * The action to call when the route matches.
     */
    private final RouteAction action;

    /**
     * Create a new routing entry.
     *
     * @param name    the entry's programmer-friendly name
     * @param path    the route path
     * @param pattern the pattern that corresponds to the path
     * @param action  the function to call when the route matches
     */
    public RoutingEntry(final String name,
                        final String path,
                        final Pattern pattern,
                        final RouteAction action) {
        this.name = name;
        this.path = path;
        this.action = action;
        this.pattern = pattern;
    }



    /**
     * Compare this object to another.
     *
     * @param obj the other object
     *
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public final boolean equals(final Object obj) {
        if (!(obj instanceof RoutingEntry)) {
            return false;
        }

        RoutingEntry otherEntry = (RoutingEntry) obj;

        return Objects.equals(this.path, otherEntry.path)
            && Objects.equals(this.name, otherEntry.name)
            && Objects.equals(this.pattern, otherEntry.pattern)
            && Objects.equals(this.action, otherEntry.action);
    }

    /**
     * Calculate the object's hash code.
     *
     * @return the hash code
     */
    @Override
    public final int hashCode() {
        return Objects.hash(this.path, this.name, this.pattern, this.action);
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
    public final int compareTo(final RoutingEntry o) {
        return this.name.compareTo(o.name);
    }
}
