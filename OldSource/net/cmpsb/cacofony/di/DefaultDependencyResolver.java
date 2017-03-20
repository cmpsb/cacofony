package net.cmpsb.cacofony.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The framework's default dependency resolver.
 *
 * @author Luc Everse
 */
public class DefaultDependencyResolver implements DependencyResolver {
    private static final Logger logger = LoggerFactory.getLogger(DefaultDependencyResolver.class);

    /**
     * The current list of instances.
     */
    private final Map<Class, Object> instances = new HashMap<>();

    /**
     * A mapping between given names and instances.
     */
    private final Map<String, Object> namedInstances = new HashMap<>();

    /**
     * Create a new dependency resolver.
     * This will also list the dependency resolver in itself.
     */
    public DefaultDependencyResolver() {
        this.add(this, DependencyResolver.class);
    }

    /**
     * Look up a dependency by name.
     * The name will be automatically converted to lower case.
     *
     * @param name the dependency name
     * @param type the dependency type
     *
     * @return an instance of the dependency
     *
     * @throws UnresolvableDependencyException if one or more dependencies could not be satisfied
     */
    @Override
    public <T> T get(final String name, final Class<T> type)
            throws UnresolvableDependencyException {

        final String canonicalName = name.toLowerCase();

        if (!this.namedInstances.containsKey(canonicalName)) {
            T instance = this.instantiate(type);
            this.add(instance, type);
            this.namedInstances.put(canonicalName, instance);
        }

        return type.cast(this.namedInstances.get(canonicalName));
    }

    /**
     * Look up a dependency by its type.
     *
     * Types being subclassed may act finicky, since this tries to match to the exact type.
     *
     * @param type the dependency type
     *
     * @return an instance of the dependency
     *
     * @throws UnresolvableDependencyException if one or more dependencies could not be satisfied
     */
    @Override
    public <T> T get(final Class<T> type) throws UnresolvableDependencyException {
        if (!this.instances.containsKey(type)) {
            T instance = this.instantiate(type);
            this.add(instance, type);
            this.namedInstances.put(type.getCanonicalName().toLowerCase(), instance);
        }

        return type.cast(this.instances.get(type));
    }

    /**
     * Statically add an instance to the resolver.
     *
     * @param instance the instance to add
     */
    @Override
    public <T> void add(final T instance) {
        this.instances.put(instance.getClass(), instance);
        this.namedInstances.put(instance.getClass().getCanonicalName().toLowerCase(), instance);
    }

    /**
     * Statically add an instance to the resolver and set the type under which is should be found.
     * The alias type must the instance type's superclass.
     *
     * @param instance  the instance
     * @param aliasType the type under which the instance should be listed
     */
    @Override
    public <S, T extends S> void add(final T instance, final Class<S> aliasType) {
        this.instances.put(aliasType, instance);
        this.namedInstances.put(aliasType.getCanonicalName().toLowerCase(), instance);

        this.add(instance);
    }

    /**
     * Assign a name to a type.
     *
     * @param type the type to assign the name to
     * @param name the name of the instance
     */
    @Override
    public <T> void name(final Class<T> type, final String name) {
        this.namedInstances.put(name.toLowerCase(), this.instances.get(type));
    }

    /**
     * Try to instantiate an object of a given type.
     *
     * @param type the class to instantiate
     * @param <T>  the class to instantiate
     *
     * @return an instance of the given type
     *
     * @throws UnresolvableDependencyException if the type couldn't be instantiated
     */
    private <T> T instantiate(final Class<T> type) throws UnresolvableDependencyException {
        logger.debug("Instantiating a {}.", type.getCanonicalName());

        // Use the plain constructor if it's available.
        try {
            return type.newInstance();
        } catch (final InstantiationException ex) {
            logger.warn("Can't instantiate the {}: not an instantiable class.",
                    type.getCanonicalName(), ex);
        } catch (final IllegalAccessException ex) {
            logger.warn("Can't instantiate the {}: class or constructor is private.",
                    type.getCanonicalName(), ex);
        }

        for (Constructor<?> ctor : type.getConstructors()) {
            try {
                Object[] parameters = new Object[ctor.getParameters().length];

                Arrays.stream(ctor.getParameterTypes())
                        .parallel()
                        .map(this::get)
                        .collect(Collectors.toList())
                        .toArray(parameters);

                return type.cast(ctor.newInstance(parameters));
            } catch (final InstantiationException ex) {
                logger.warn("Can't instantiate the {}: not an instantiable class.",
                            type.getCanonicalName(), ex);
            } catch (final IllegalAccessException ex) {
                logger.warn("Can't instantiate the {}: class or constructor is private.",
                            type.getCanonicalName(), ex);
            } catch (final InvocationTargetException ex) {
                logger.warn("Can't instantiate the {}: exception in constructor.",
                            type.getCanonicalName(), ex);
            }
        }

        throw new UnresolvableDependencyException("All constructors exhausted.");
    }
}
