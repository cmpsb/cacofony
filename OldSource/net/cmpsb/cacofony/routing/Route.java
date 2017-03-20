package net.cmpsb.cacofony.routing;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Luc Everse
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {

    /**
     * @return the path specification the action serves
     */
    String path();
}
