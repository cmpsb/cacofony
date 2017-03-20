package net.cmpsb.cacofony.routing;

import net.cmpsb.cacofony.http.Method;
import net.cmpsb.cacofony.response.ResponseTransformer;

import java.util.List;
import java.util.Objects;

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
     * The route's compiled path.
     */
    private final CompiledPath path;

    /**
     * The action to call when the route matches.
     */
    private final RouteAction action;

    /**
     * The transformer to apply to the response.
     */
    private final ResponseTransformer transformer;

    /**
     * The HTTP method this route accepts.
     */
    private final List<Method> methods;

    /**
     * Create a new routing entry.
     *
     * @param name        the entry's programmer-friendly name
     * @param path        the route path
     * @param action      the function to call when the route matches
     * @param transformer the output transformer to use
     * @param methods     the HTTP methods this route serves
     */
    public RoutingEntry(final String name,
                        final CompiledPath path,
                        final RouteAction action,
                        final ResponseTransformer transformer,
                        final List<Method> methods) {
        this.name = name;
        this.path = path;
        this.action = action;
        this.transformer = transformer;
        this.methods = methods;
    }

    /**
     * @return the programmer-friendly route name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the input path this entry represents
     */
    public CompiledPath getPath() {
        return this.path;
    }

    /**
     * @return the action to call if the route matches
     */
    public RouteAction getAction() {
        return this.action;
    }

    /**
     * @return the output transformer to apply
     */
    public ResponseTransformer getTransformer() {
        return this.transformer;
    }

    /**
     * @return the HTTP methods this route serves
     */
    public List<Method> getMethods() {
        return this.methods;
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
            && Objects.equals(this.action, otherEntry.action)
            && Objects.equals(this.transformer, otherEntry.transformer)
            && Objects.equals(this.methods, otherEntry.methods);
    }

    /**
     * Calculate the object's hash code.
     *
     * @return the hash code
     */
    @Override
    public final int hashCode() {
        return Objects.hash(this.path, this.name, this.action, this.transformer, this.methods);
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
