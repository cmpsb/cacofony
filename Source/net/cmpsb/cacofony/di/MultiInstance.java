package net.cmpsb.cacofony.di;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation indicating that the dependency resolver must not cache instances and create
 * new ones instead for every dependency to satisfy.
 *
 * @author Luc Everse
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MultiInstance {
}
