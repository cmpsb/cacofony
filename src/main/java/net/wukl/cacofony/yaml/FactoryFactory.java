package net.wukl.cacofony.yaml;

import net.wukl.cacodi.Factory;

import java.util.Map;

/**
 * A factory that builds DI factories based on a YAML configuration.
 *
 * @param <T> the type of factory the factory creates
 *
 * @author Luc Everse
 */
@FunctionalInterface
public interface FactoryFactory<T> {
    /**
     * Builds the factory.
     *
     * @param options any YAML options
     *
     * @return a factory
     */
    Factory<T> build(Map<String, Object> options);
}
