package net.cmpsb.cacofony.di.classes;


/**
 * @author Luc Everse
 */
public class ComplexNested extends Complex {
    public ComplexNested(final NullaryConstructor one,
                         final SimpleImplicit other,
                         final Integer count) {
        super(one, other, count);
    }
}
