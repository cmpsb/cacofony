package net.cmpsb.cacofony.di;

import net.cmpsb.cacofony.util.Ob;
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
import java.util.Set;

/**
 * The framework's default dependency resolver.
 *
 * @author Luc Everse
 */
public class DefaultDependencyResolver extends DependencyResolver {
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
     * All injection strategies that cause the resolver to look the dependency up by name.
     */
    private final Set<String> byNameStrategies = Ob.set("name");

    /**
     * All injection strategies that cause the resolver to look for the dependency in the arguments.
     */
    private final Set<String> inArgStrategies = Ob.set("arg", "argument", "param", "parameter");

    /**
     * Creates a new dependency resolver.
     * This will also list the dependency resolver in itself.
     */
    public DefaultDependencyResolver() {
        this.add(this, DependencyResolver.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T get(final String name, final Map<String, ?> arguments, final Class<T> type)
            throws UnresolvableDependencyException {

        final String canonicalName = name.toLowerCase();

        if (!this.namedInstances.containsKey(canonicalName)) {
            T instance = this.instantiate(type, arguments);
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
    public <T> T get(final Map<String, ?> arguments, final Class<T> type)
            throws UnresolvableDependencyException {
        if (!this.mayCache(type)) {
            return this.instantiate(type, arguments);
        }

        if (!this.instances.containsKey(type)) {
            T instance = this.instantiate(type, arguments);
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
     * Add a named object to the resolver and don't associate it with any type.
     *
     * @param name   the name of the dependency
     * @param object the dependency
     */
    @Override
    public void add(final String name, final Object object) {
        this.namedInstances.put(name.toLowerCase(), object);
    }

    /**
     * Try to instantiate an object of a given type.
     *
     * @param type      the class to instantiate
     * @param arguments any runtime arguments to pass to the classes to instantiate
     * @param <T>  the class to instantiate
     *
     * @return an instance of the given type
     *
     * @throws UnresolvableDependencyException if the type couldn't be instantiated
     */
    private <T> T instantiate(final Class<T> type,
                              final Map<String, ?> arguments) {
        logger.debug("Instantiating a {}.", type.getCanonicalName());

        // Use the plain constructor if it's available.
        try {
            final T obj =  type.newInstance();

            this.populateFields(obj, type, arguments);

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
                final T obj = this.construct(ctor, arguments, type);

                this.populateFields(obj, type, arguments);

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
     * @param arguments any runtime arguments
     * @param type      the dependency's type
     * @param <T>       the dependency's type
     *
     * @return an instance of the dependency
     *
     * @throws IllegalAccessException    if the constructor is inaccessible, i.e. private
     * @throws InvocationTargetException if the constructor raised an exception
     * @throws InstantiationException    if the dependency is not an instantiable class
     */
    private <T> T construct(final Constructor<?> ctor,
                            final Map<String, ?> arguments,
                            final Class<T> type)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        final Parameter[] parameters = ctor.getParameters();
        final int numParameters = parameters.length;
        final Object[] values = new Object[numParameters];

        for (int i = 0; i < numParameters; ++i) {
            values[i] = this.getForParameter(parameters[i], arguments);
        }

        return type.cast(ctor.newInstance(values));
    }

    /**
     * Resolves a single dependency for a possibly annotated parameter.
     *
     * @param param     the parameter to resolve for
     * @param arguments any runtime arguments for this resolving session
     *
     * @return a dependency that satisfies the selected parameter
     *
     * @throws UnknownInjectionStrategyException if the parameter is annotated with an unknown
     *                                           strategy
     */
    private Object getForParameter(final Parameter param,
                                   final Map<String, ?> arguments) {
        final Class<?> type = param.getType();
        final Inject annotation = param.getAnnotation(Inject.class);

        return this.getForAnnotation(annotation, type, arguments);
    }

    /**
     * Inject any dependencies in an object's annotated fields.
     *
     * @param obj       the object instance to modify
     * @param type      the type of the object instance
     * @param arguments any invocation-time arguments
     * @param <T>       the type of the object instance
     *
     * @throws IllegalAccessException if at least one field cannot be modified
     */
    private <T> void populateFields(final T obj,
                                    final Class<T> type,
                                    final Map<String, ?> arguments) throws IllegalAccessException {
        final List<Field> fields = this.collectInjectableFields(type);

        for (final Field field : fields) {
            final Inject annotation = field.getAnnotation(Inject.class);
            final Class<?> fieldType = field.getType();
            final Object instance = this.getForAnnotation(annotation, fieldType, arguments);

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
     * Resolves a single dependency for a possibly annotated element.
     * <p>
     * Unlike the name implies, the element does not necessarily have to be annotated.
     *
     * @param annotation the annotation or {@code null} if the element is unannotated
     * @param type       the dependency's type
     * @param arguments  any runtime arguments
     * @param <T>        the dependency's type
     *
     * @return a dependency that satisfies the type and, if applicable, the annotation
     */
    private <T> T getForAnnotation(final Inject annotation,
                                   final Class<T> type,
                                   final Map<String, ?> arguments) {
        // If there is no parameter, assume inferred injection.
        if (annotation == null || annotation.value().equals("(infer)")) {
            return this.get(arguments, type);
        }

        // Otherwise look at the left-hand side
        final String injectSpec = annotation.value();
        final int colonPosition = injectSpec.indexOf(':');
        final String strategy = injectSpec.substring(0, colonPosition);
        final String injectionName = injectSpec.substring(colonPosition + 1).trim();

        if (this.byNameStrategies.contains(strategy)) {
            return this.get(injectionName, arguments, type);
        } else if (this.inArgStrategies.contains(strategy)) {
            return type.cast(arguments.get(injectionName));
        }

        throw new UnknownInjectionStrategyException(
                "Unknown injection strategy \"" + strategy + "\"."
        );
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
     * Checks whether a type can be cached after instantiating it.
     *
     * @param type the type to check
     * @param <T>  the type to check
     *
     * @return true if instances of this type can be cached, false otherwise
     */
    private <T> boolean mayCache(final Class<T> type) {
        return !(type.isAnnotationPresent(MultiInstance.class));
    }
}
