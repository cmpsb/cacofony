package net.wukl.cacofony.server.host;

/**
 * A controller package.
 *
 * @author Luc Everse
 */
public class ControllerPackage {
    /**
     * The name of the package to scan for controller.
     */
    private final String pack;

    /**
     * The path prefix for the controllers in the package.
     */
    private final String prefix;

    /**
     * Creates a new controller package.
     *
     * @param pack   the name of the package to scan
     * @param prefix the URL prefix for the paths the controllers serve
     */
    public ControllerPackage(final String pack, final String prefix) {
        this.pack = pack;
        this.prefix = prefix;
    }

    /**
     * Creates a new controller package.
     *
     * @param pack the name of the package to scan.
     */
    public ControllerPackage(final String pack) {
        this(pack, "");
    }

    /**
     * Returns the name of the package to scan.
     *
     * @return the name of the package to scan
     */
    public String getPack() {
        return this.pack;
    }

    /**
     * Returns the URL prefix for the controller paths.
     *
     * @return the URL prefix
     */
    public String getPrefix() {
        return this.prefix;
    }
}
