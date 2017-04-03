package net.cmpsb.cacofony.di.classes;

import net.cmpsb.cacofony.di.Inject;

/**
 * @author Luc Everse
 */
public class ShitloadOfAnnotations {
    private final String value;

    public ShitloadOfAnnotations(@Inject("name:named") final String named,
                                 @Inject("arg:one") final String one,
                                 @Inject("argument: two") final String two,
                                 @Inject("param:three") final String three,
                                 @Inject("parameter:four") final String four) {
        this.value = named.hashCode() + one + two + three + four;
    }

    public String getValue() {
        return this.value;
    }
}
