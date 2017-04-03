package net.cmpsb.cacofony.di.classes;

import net.cmpsb.cacofony.di.Inject;

/**
 * @author Luc Everse
 */
public class ComplexNested extends Complex {
    public ComplexNested(final NullaryConstructor one,
                         @Inject("name: test.multi") final SimpleImplicit other,
                         @Inject("argument: count") final Integer count) {
        super(one, other, count);
    }
}
