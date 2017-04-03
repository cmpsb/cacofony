package net.cmpsb.cacofony.route;

import net.cmpsb.cacofony.http.request.Method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Luc Everse
 */
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Routes.class)
@Target(ElementType.METHOD)
public @interface Route {
    /**
     * @return the path specification the action serves
     */
    String path();

    /**
     * @return the requirements the path parameters should comply with
     */
    Requirement[] requirements() default {};

    /**
     * @return the methods the route applies to
     */
    Method[] methods() default {Method.GET};

    /**
     * @return the content types the route is willing to serve
     */
    String[] types() default {"*/*"};

    /**
     * @return a human-readable name for the route
     */
    String name() default "";
}
