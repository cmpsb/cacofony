package net.cmpsb.cacofony.di.classes;

/**
 * @author Luc Everse
 */
public class RiskyConstructor {
    public RiskyConstructor(final NullaryConstructor dep) {
        throw new RuntimeException("risk with dependency: " + dep);
    }
}
