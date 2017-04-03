package net.cmpsb.cacofony.di.classes;

/**
 * @author Luc Everse
 */
public class SimpleImplicit {
    private final NullaryConstructor left;
    private final NullaryConstructor right;

    public SimpleImplicit(final NullaryConstructor left, final NullaryConstructor right) {
        this.left = left;
        this.right = right;
    }

    public NullaryConstructor getLeft() {
        return this.left;
    }

    public NullaryConstructor getRight() {
        return this.right;
    }
}
