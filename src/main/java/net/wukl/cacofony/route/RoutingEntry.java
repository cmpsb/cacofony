package net.wukl.cacofony.route;

import net.wukl.cacofony.controller.Controller;
import net.wukl.cacofony.http.request.Method;
import net.wukl.cacofony.mime.MimeType;

import java.lang.reflect.InvocationTargetException;
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
     * The controller to invoke the method upon.
     */
    private final Controller controller;

    /**
     * The method to call when the route matches.
     */
    private final java.lang.reflect.Method method;

    /**
     * The HTTP method this route accepts.
     */
    private final List<Method> methods;

    /**
     * The content types this route can serve.
     */
    private final List<MimeType> contentTypes;

    /**
     * Create a new routing entry.
     *
     * @param name         the entry's programmer-friendly name
     * @param path         the route path
     * @param controller   the controller to invoke the method upon
     * @param method       the function to call when the route matches
     * @param methods      the HTTP methods this route serves
     * @param contentTypes the MIME types the route can serve
     */
    public RoutingEntry(final String name,
                        final CompiledPath path,
                        final Controller controller,
                        final java.lang.reflect.Method method,
                        final List<Method> methods,
                        final List<MimeType> contentTypes) {
        this.name = name;
        this.path = path;
        this.controller = controller;
        this.method = method;
        this.methods = methods;
        this.contentTypes = contentTypes;
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
     * Returns the controller to invoke the method upon.
     *
     * @return the controller to invoke the method upon
     */
    public Controller getController() {
        return this.controller;
    }

    /**
     * @return the method to call if the route matches
     */
    public java.lang.reflect.Method getMethod() {
        return this.method;
    }

    /**
     * @return the HTTP methods this route serves
     */
    public List<Method> getMethods() {
        return this.methods;
    }

    /**
     * @return the MIME types the route can serve
     */
    public List<MimeType> getContentTypes() {
        return this.contentTypes;
    }

    /**
     * Invokes the routing entry with the given arguments.
     *
     * @param args the method arguments
     *
     * @return the response
     *
     * @throws Exception any exception
     */
    public Object invoke(final Object... args) throws Exception {
        try {
            return this.method.invoke(this.controller, args);
        } catch (final InvocationTargetException ex) {

            // Unpack the cause if there is a valid one.
            // Throwables are skipped to prevent having to modify the entire invocation chain.
            final Throwable cause = ex.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }

            throw ex;
        }
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
            && Objects.equals(this.controller, otherEntry.controller)
            && Objects.equals(this.method, otherEntry.method)
            && Objects.equals(this.methods, otherEntry.methods)
            && Objects.equals(this.contentTypes, otherEntry.contentTypes);
    }

    /**
     * Calculate the object's hash code.
     *
     * @return the hash code
     */
    @Override
    public final int hashCode() {
        return Objects.hash(
                this.path, this.name, this.controller, this.method, this.methods, this.contentTypes
        );
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
