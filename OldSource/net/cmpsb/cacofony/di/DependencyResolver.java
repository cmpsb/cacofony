package net.cmpsb.cacofony.di;

/**
 * A dependency resolver.
 *
 * @author Luc Everse
 */
public interface DependencyResolver {

    /**
     * Look up a dependency by name.
     *
     * @param name the dependency name
     * @param type the dependency type
     * @param <T>  the dependency type
     *
     * @return an instance of the dependency
     *
     * @throws UnresolvableDependencyException if one or more dependencies could not be satisfied
     */
    <T> T get(String name, Class<T> type) throws UnresolvableDependencyException;

    /**
     * Look up a dependency by its type.
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
    <T> T get(Class<T> type) throws UnresolvableDependencyException;

    /**
     * Statically add an instance to the resolver.
     *
     * @param instance the instance to add
     * @param <T>      the instance's type
     */
    <T> void add(T instance);

    /**
     * Statically add an instance to the resolver and set the type under which is should be found.
     * The alias type must the instance type's superclass.
     *
     * @param instance  the instance
     * @param aliasType the type under which the instance should be listed
     * @param <T>       the instance's type
     * @param <S>       the alias type
     */
    <S, T extends S> void add(T instance, Class<S> aliasType);

    /**
     * Assign a name to a type.
     *
     * @param type the type to assign the name to
     * @param name the name of the instance
     *
     * @param <T> the type to assign the name to
     */
    <T> void name(Class<T> type, String name);
}
