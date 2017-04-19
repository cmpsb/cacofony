package net.cmpsb.cacofony.di;

/**
 * A class marking another class as an injectable service.
 * <p>
 * A service is a class directly usable in a DI context. Each service has direct access to a
 * dependency resolver and can receive fixed dependencies through its constructor or fields.
 * <p>
 * Services not constructed through a resolver will probably crash the application, so beware.
 *
 * @author Luc Everse
 */
public abstract class Service {
    /**
     * The dependency resolver to use.
     */
    @Inject private DependencyResolver resolver;

    /**
     * Returns the dependency resolver through which it was instantiated.
     *
     * @return the dependency resolver
     */
    public DependencyResolver getResolver() {
        return this.resolver;
    }

    /**
     * Looks up a service by its interface.
     *
     * @param type the class or interface of the service
     * @param <T>  the class or interface of the service
     *
     * @return an instance of the service
     */
    public <T> T get(final Class<T> type) {
        return this.resolver.get(type);
    }
}
