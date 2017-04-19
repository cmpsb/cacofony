package net.cmpsb.cacofony.di.classes;

import net.cmpsb.cacofony.di.Inject;

/**
 * @author Luc Everse
 */
public class Complex {
    private final NullaryConstructor one;
    private final SimpleImplicit other;
    private final int count;

    @Inject
    private final String fieldDep = null;

    public Complex(final NullaryConstructor one, final SimpleImplicit other,
                   final Integer count) {
        this.one = one;
        this.other = other;
        this.count = count;
    }

    public NullaryConstructor getOne() {
        return this.one;
    }

    public SimpleImplicit getOther() {
        return this.other;
    }

    public String getFieldDep() {
        return this.fieldDep;
    }

    public int getCount() {
        return this.count;
    }
}
