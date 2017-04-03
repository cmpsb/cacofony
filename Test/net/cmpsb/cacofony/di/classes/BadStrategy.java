package net.cmpsb.cacofony.di.classes;

import net.cmpsb.cacofony.di.Inject;

/**
 * @author Luc Everse
 */
public class BadStrategy {
    public BadStrategy(@Inject("bad strategy: strategy harder") final String dep) {
    }
}
