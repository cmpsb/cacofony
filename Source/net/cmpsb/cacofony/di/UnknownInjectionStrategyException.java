package net.cmpsb.cacofony.di;

/**
 * An exception signaling an unknown injection strategy. This is raised when a parameter or
 * field is annotated with @{@link Inject} and the value does not resolve to anything the resolver
 * can understand.
 * <p>
 * An example case is as follows:
 * <pre>
 * {@code
 *  public class BrokenClass {
 *      public BrokenClass(@Inject("nonexistent strategy: this breaks") final Service service) {}
 *  }
 * }
 * </pre>
 * Requesting an instance of {@code BrokenClass} will fail because there is no strategy named
 * {@code nonexistent strategy}.
 *
 * @author Luc Everse
 */
public class UnknownInjectionStrategyException extends RuntimeException {
    /**
     * Creates a new unknown injection strategy exception.
     *
     * @param message a message detailing the problem
     */
    public UnknownInjectionStrategyException(final String message) {
        super(message);
    }
}
