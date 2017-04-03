package net.cmpsb.cacofony.di;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Luc Everse
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Inject {
    /**
     * Returns the strategy and name of the dependency to inject.
     * <p>
     * The default is {@code (infer)}, which causes the resolver to look up the instance by the
     * field or parameter type.
     * <p>
     * Alternatively, if the value starts with {@code name:}, then the remaining string will be
     * used to look up a dependency by name. The type will not be checked.
     * <p>
     * A third option is {@code arg:}, {@code argument:}, {@code param:} or {@code parameter:},
     * which causes the resolver to look for that dependency in its invocation parameter map.
     * Try to limit the usage of this form, because if your class is somewhere down the dependency
     * chain it may not be clear that such an argument is needed.
     *
     * @return the name of the dependency to inject
     */
    String value() default "(infer)";
}
