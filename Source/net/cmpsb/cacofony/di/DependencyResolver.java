package net.cmpsb.cacofony.di;

import net.cmpsb.cacofony.exception.DefaultExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A dependency resolver.
 *
 * @author Luc Everse
 */
public class DependencyResolver {
    private static final Logger logger = LoggerFactory.getLogger(DependencyResolver.class);
    /**
     * The current list of instances.
     */
    private final Map<Class, Object> instances;
    /**
     * A mapping between interfaces and their implementing classes.
     */
    private final Map<Class, Class> implementations;

    /**
     * The factories to consult.
     */
    private final Map<Class, Factory<?>> factories;

    /**
     * Creates a new dependency resolver.
     * This will also list the dependency resolver in itself.
     */
    public DependencyResolver() {
        this.instances = new HashMap<>();
        this.implementations = new HashMap<>();
        this.factories = new HashMap<>();

        this.implementations.put(DependencyResolver.class, DefaultExceptionHandler.class);
        this.instances.put(DependencyResolver.class, this);
    }

    /**
     * Creates a new dependency resolver as a clone of another.
     *
     * @param other the source resolver
     */
    public DependencyResolver(final DependencyResolver other) {
        this.instances = new HashMap<>(other.instances);
        this.implementations = new HashMap<>(other.implementations);
        this.factories = new HashMap<>(other.factories);

        this.instances.put(DependencyResolver.class, this);
    }

    /**
     * Try to instantiate an object of a given type.
     *
     * @param type      the class to instantiate
     * @param <T>  the class to instantiate
     *
     * @return an instance of the given type
     *
     * @throws UnresolvableDependencyException if the type couldn't be instantiated
     */
    @SuppressWarnings("unchecked")
    private <T> T instantiate(final Class<T> type) {
        logger.debug("Instantiating a {}.", type.getCanonicalName());

        // Use the factory or plain constructor if it's available.
        try {
            final T obj;

            final Factory<T> factory = (Factory<T>) this.factories.get(type);
            if (factory != null) {
                obj = factory.build(this);
            } else {
                obj = type.newInstance();
            }

            this.populateFields(obj, type);

            logger.debug("Successfully instantiated a {}.", type.getCanonicalName());
            return obj;
        } catch (final InstantiationException ex) {
            logger.debug("Can't instantiate the {} via the nullary constructor: "
                         + "not available or not instantiable.",
                         type.getCanonicalName());
        } catch (final IllegalAccessException ex) {
            logger.debug("Can't instantiate the {} via the nullary constructor: "
                         + "class or constructor is private.",
                         type.getCanonicalName());
        }

        for (final Constructor<?> ctor : type.getConstructors()) {
            try {
                final T obj = this.construct(ctor, type);

                this.populateFields(obj, type);

                logger.debug("Successfully instantiated a {}.", type.getCanonicalName());
                return obj;
            } catch (final InstantiationException ex) {
                logger.debug("Can't instantiate the {}: not an instantiable class.",
                             type.getCanonicalName());
            } catch (final IllegalAccessException ex) {
                logger.debug("Can't instantiate the {}: class or constructor is private.",
                             type.getCanonicalName());
            } catch (final InvocationTargetException ex) {
                logger.debug("Can't instantiate the {}: exception in constructor: ",
                             type.getCanonicalName(), ex);
            }
        }

        logger.debug("Can't instantiate the {}: all constructors exhausted.",
                     type.getCanonicalName());
        throw new UnresolvableDependencyException("All constructors exhausted.");
    }

    /**
     * Try to construct the dependency using the given constructor.
     *
     * @param ctor      the constructor to try
     * @param type      the dependency's type
     * @param <T>       the dependency's type
     *
     * @return an instance of the dependency
     *
     * @throws IllegalAccessException    if the constructor is inaccessible, i.e. private
     * @throws InvocationTargetException if the constructor raised an exception
     * @throws InstantiationException    if the dependency is not an instantiable class
     */
    private <T> T construct(final Constructor<?> ctor, final Class<T> type)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        final Parameter[] parameters = ctor.getParameters();
        final int numParameters = parameters.length;
        final Object[] values = new Object[numParameters];

        for (int i = 0; i < numParameters; ++i) {
            values[i] = this.get(parameters[i].getType());
        }

        return type.cast(ctor.newInstance(values));
    }

    /**
     * Inject any dependencies in an object's annotated fields.
     *
     * @param obj       the object instance to modify
     * @param type      the type of the object instance
     * @param <T>       the type of the object instance
     *
     * @throws IllegalAccessException if at least one field cannot be modified
     */
    private <T> void populateFields(final T obj,
                                    final Class<T> type) throws IllegalAccessException {
        final List<Field> fields = this.collectInjectableFields(type);

        for (final Field field : fields) {
            final Class<?> fieldType = field.getType();
            final Object instance = this.get(fieldType);

            final boolean isAccessible = field.isAccessible();
            try {
                if (!isAccessible) {
                    field.setAccessible(true);
                }

                field.set(obj, instance);
            } catch (final SecurityException ex) {
                logger.warn("Unable to make {} accessible. Injection may fail.",
                        field.toString());
            } finally {
                // Restore the inaccessibility when we're done, even after an exception.
                if (!isAccessible) {
                    field.setAccessible(false);
                }
            }
        }
    }

    /**
     * Recursively collects all fields that are eligible for dependency injection in a class.
     *
     * @param type the type to collect the fields for
     * @param <T>  the type to collect the fields for
     *
     * @return a list of all injectable fields
     */
    private <T> List<Field> collectInjectableFields(final Class<T> type) {
        final List<Field> fields = new ArrayList<>();

        for (final Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                fields.add(field);
            }
        }

        final Class<? super T> superClass = type.getSuperclass();
        if (superClass != null) {
            fields.addAll(this.collectInjectableFields(superClass));
        }

        return fields;
    }

    /**
     * Looks up a dependency by its type.
     * <p>
     * Types being subclassed may act finicky, since this tries to match to the exact type.
     *
     * @param type the dependency type
     * @param <T>  the dependency type
     * @return an instance of the dependency
     * @throws UnresolvableDependencyException if one or more dependencies could not be satisfied
     */
    @SuppressWarnings("unchecked")
    public <T> T get(final Class<T> type) {
        // If there's a cached version ready, return that.
        final Object cachedInstance = this.instances.get(type);
        if (cachedInstance != null) {
            return type.cast(cachedInstance);
        }

        final Class<? extends T> implType;
        if (this.factories.containsKey(type)) {
            // Use the factory if one is registered.
            implType = type;
        } else {
            // Otherwise look for the implementor class.
            implType = this.implementations.getOrDefault(type, type);
        }

        final T instance = this.instantiate(implType);
        this.instances.put(type, instance);
        return instance;
    }

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
    public <S, T extends S> void add(final Class<S> iface, final T impl) {
        this.implementations.put(iface, impl.getClass());
        this.instances.put(iface, impl);
    }

    /**
     * Registers a factory for a service.
     *
     * @param iface   the interface the instance implements
     * @param factory the factory
     * @param <S>     the type of the interface
     */
    public <S> void addFactory(final Class<S> iface, final Factory<S> factory) {
        this.factories.put(iface, factory);
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
    public <S, T extends S> void addDefault(final Class<S> iface, final T impl) {
        if (!this.implementations.containsKey(iface)) {
            this.add(iface, impl);
        }
    }

    /**
     * Registers a factory for a service, but only if there was no other factory in place.
     *
     * @param iface   the interface the instance implements
     * @param factory the factory
     * @param <S>     the type of the interface
     */
    public <S> void addDefaultFactory(final Class<S> iface, final Factory<S> factory) {
        if (!this.factories.containsKey(iface)) {
            this.factories.put(iface, factory);
        }
    }

    /**
     * Registers an interface implementation.
     *
     * @param iface the interface of the implementor
     * @param impl  the implementor class
     * @param <T>   the type of the interface
     * @param <S>   the type of the implementation
     */
    public <S, T extends S> void implement(final Class<S> iface, final Class<T> impl) {
        this.implementations.put(iface, impl);
    }

    /**
     * Registers an interface implementation, but only if there was no implementor in place.
     *
     * @param iface the interface of the implementor
     * @param impl  the implementor class
     * @param <T>   the type of the interface
     * @param <S>   the type of the implementation
     */
    public <S, T extends S> void implementDefault(final Class<S> iface, final Class<T> impl) {
        if (!this.implementations.containsKey(iface)) {
            this.implement(iface, impl);
        }
    }
}
