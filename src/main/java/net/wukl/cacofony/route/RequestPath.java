package net.wukl.cacofony.route;

/**
 * Helper class for route paths.
 *
 * @author Luc Everse
 */
public final class RequestPath {
    /**
     * The default requirement regex for a route parameter.
     */
    public static final String DEFAULT_PARAMETER = PathCompiler.PARAMETER_VALUE_PATTERN;

    /**
     * A pattern matching any valid parameter or no parameter at all.
     */
    public static final String OPTIONAL_PARAMETER = "(" + DEFAULT_PARAMETER + ")?";

    /**
     * Do not instantiate.
     */
    private RequestPath() {
        throw new AssertionError("Do not instantiate the RequestPath helper class.");
    }
}
