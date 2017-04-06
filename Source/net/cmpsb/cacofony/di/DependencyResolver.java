package net.cmpsb.cacofony.di;

import java.util.Collections;
import java.util.Map;

/**
 * A dependency resolver.
 *
 * @author Luc Everse
 */
public abstract class DependencyResolver {

    /**
     * Looks up a dependency by name.
     *
     * @param name      the dependency name
     * @param arguments any runtime arguments
     * @param type      the dependency type
     * @param <T>       the dependency type
     *
     * @return an instance of the dependency
     *
     * @throws UnresolvableDependencyException if one or more dependencies could not be satisfied
     */
    public abstract <T> T get(String name, Map<String, ?> arguments, Class<T> type)
            throws UnresolvableDependencyException;

    /**
     * Looks up a dependency by name.
     *
     * @param name the dependency name
     * @param type the dependency type
     * @param <T>  the dependency type
     *
     * @return an instance of the dependency
     *
     * @throws UnresolvableDependencyException if one or more dependencies could not be satisfied
     */
    public <T> T get(final String name, final Class<T> type) {
        return this.get(name, Collections.emptyMap(), type);
    }

    /**
     * Looks up a dependency by its type.
     *
     * Types being subclassed may act finicky, since this tries to match to the exact type.
     *
     * @param type      the dependency type
     * @param arguments any runtime arguments
     * @param <T>       the dependency type
     *
     * @return an instance of the dependency
     *
     * @throws UnresolvableDependencyException if one or more dependencies could not be satisfied
     */
    public abstract <T> T get(Map<String, ?> arguments, Class<T> type)
            throws UnresolvableDependencyException;

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
    public <T> T get(final Class<T> type) {
        return this.get(Collections.emptyMap(), type);
    }

    /**
     * Statically adds an instance to the resolver.
     *
     * @param instance the instance to add
     * @param <T>      the instance's type
     */
    public abstract <T> void add(T instance);

    /**
     * Statically adds an instance to the resolver and set the type under which is should be found.
     * <p>
     * The alias type must the instance type's superclass.
     *
     * @param instance  the instance
     * @param aliasType the type under which the instance should be listed
     * @param <T>       the instance's type
     * @param <S>       the alias type
     */
    public abstract <S, T extends S> void add(T instance, Class<S> aliasType);

    /**
     * Assigns a name to a type.
     *
     * @param type the type to assign the name to
     * @param name the name of the instance
     *
     * @param <T> the type to assign the name to
     */
    public abstract <T> void name(Class<T> type, String name);

    /**
     * Looks up a dependency by its type and registers an alias for it.
     * <p>
     * The alias type must be a superclass of {@code type}.
     *
     * @param type      the type to construct
     * @param aliasType the alias type
     *
     * @param <S> the type to construct
     * @param <T> the alias type
     *
     * @return the instance
     */
    public <S, T extends S> T get(final Class<T> type, final Class<S> aliasType) {
        final T instance = this.get(type);
        this.add(instance, aliasType);
        return instance;
    }

    /**
     * Add a named object to the resolver and don't associate it with any type.
     *
     * @param name   the name of the dependency
     * @param object the dependency
     */
    public abstract void add(String name, Object object);

    /**
     * Returns whether a type (either directly or as an alias) is present in the resolver's cache.
     *
     * @param type the type to look for
     *
     * @return true if the type is present, otherwise false
     */
    public abstract boolean isKnown(Class<?> type);

    /**
     * Returns whether a named instance is present in the resolver's mapping.
     *
     * @param name the name of the instance to look for
     *
     * @return true if there's a type with that name, otherwise false
     */
    public abstract boolean isKnown(String name);

    /**
     * Removes the instance of that type (if there is any) from the resolver cache.
     *
     * @param type the type to forget
     * @param <T>  the type to forget
     *
     * @return the instance that was at that spot or {@code null} if the resolver didn't know it
     */
    public abstract <T> T forget(Class<T> type);

    /**
     * Removes the instance with that name (if there is any) from the resolver's mapping.
     *
     * @param name the name to forget
     *
     * @return the instance with that name or {@code null} if the resolver didn't know it
     */
    public abstract Object forget(String name);
}
