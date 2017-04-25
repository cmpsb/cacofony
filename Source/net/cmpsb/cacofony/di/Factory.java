package net.cmpsb.cacofony.di;


/**
 * A factory for building services.
 * <p>
 * You employ a factory if a service requires additional actions <em>after</em> the dependencies
 * are ready but <em>before</em> the requester may access it.
 *
 * @param <T> the type the factory constructs
 *
 * @author Luc Everse
 */
@FunctionalInterface
public interface Factory<T> {
    /**
     * Builds the service.
     *
     * @param resolver the dependency resolver invoking the factory
     *
     * @return an instance of the service
     *
     * @throws UnresolvableDependencyException if the dependency cannot be resolved
     */
    T build(DependencyResolver resolver) throws UnresolvableDependencyException;
}
