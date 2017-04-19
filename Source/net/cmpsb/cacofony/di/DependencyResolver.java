package net.cmpsb.cacofony.di;

import java.util.function.Supplier;

/**
 * A dependency resolver.
 *
 * @author Luc Everse
 */
public abstract class DependencyResolver {
    /**
     * Looks up a dependency by its type.
     *
     * Types being subclassed may act finicky, since this tries to match to the exact type.
     *
     * @param type the dependency type
     * @param <T>  the dependency type
     *
     * @return an instance of the dependency
     *
     * @throws UnresolvableDependencyException if one or more dependencies could not be satisfied
     */
    public abstract <T> T get(Class<T> type);

    /**
     * Statically adds an instance to the resolver and set the type under which is should be found.
     * <p>
     * The alias type must the instance type's superclass.
     *
     * @param iface the interface the instance implements
     * @param impl  the actual implementation
     * @param <T>   the type of the interface
     * @param <S>   the type of the implementation
     */
    public abstract <S, T extends S> void add(Class<S> iface, T impl);

    /**
     * Statically adds an instance to the resolver and set the type under which is should be found.
     * <p>
     * The alias type must the instance type's superclass.
     *
     * @param iface the interface the instance implements
     * @param impl  the actual implementation
     * @param <T>   the type of the interface
     * @param <S>   the type of the implementation
     */
    public <S, T extends S> void addSupplied(final Class<S> iface, final Supplier<T> impl) {
        this.add(iface, impl.get());
    }

    /**
     * Statically adds an instance to the resolver and set the type under which is should be found,
     * but only if there was no other implementor in place.
     * <p>
     * The alias type must the instance type's superclass.
     *
     * @param iface the interface the instance implements
     * @param impl  the actual implementation
     * @param <T>   the type of the interface
     * @param <S>   the type of the implementation
     */
    public abstract <S, T extends S> void addDefault(Class<S> iface, T impl);

    /**
     * Statically adds an instance to the resolver and set the type under which is should be found,
     * but only if there was no other implementor in place.
     * <p>
     * The alias type must the instance type's superclass.
     *
     * @param iface the interface the instance implements
     * @param impl  the actual implementation
     * @param <T>   the type of the interface
     * @param <S>   the type of the implementation
     */
    public <S, T extends S> void addDefaultSupplied(final Class<S> iface, final Supplier<T> impl) {
        this.addDefault(iface, impl.get());
    }

    /**
     * Registers an interface implementation.
     *
     * @param iface the interface of the implementor
     * @param impl  the implementor class
     * @param <S>   the type of the interface
     * @param <T>   the type of the implementor
     */
    public abstract <S, T extends S> void implement(Class<S> iface, Class<T> impl);

    /**
     * Registers an interface implementation, but only if there was no implementor in place.
     *
     * @param iface the interface of the implementor
     * @param impl  the implementor class
     * @param <S>   the type of the interface
     * @param <T>   the type of the implementor
     */
    public abstract <S, T extends S> void implementDefault(Class<S> iface, Class<T> impl);
}
