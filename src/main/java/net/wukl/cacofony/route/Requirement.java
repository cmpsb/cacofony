package net.wukl.cacofony.route;

/**
 * @author Luc Everse
 */
public @interface Requirement {
    /**
     * @return the name of the parameter
     */
    String name();

    /**
     * @return the regex the parameter should comply with
     */
    String regex();
}
