package net.cmpsb.cacofony;

import net.cmpsb.cacofony.di.DefaultDependencyResolver;
import net.cmpsb.cacofony.di.DependencyResolver;

/**
 * The server's default entry point.
 *
 * @author Luc Everse
 */
public final class Main {

    /**
     * Don't instantiate.
     * Why would you even try..
     */
    private Main() {
        throw new AssertionError("Do not instantiate the Main class.");
    }

    /**
     * The server's default entry point.
     *
     * @param args any command line arguments
     */
    public static void main(final String[] args) {
        final DependencyResolver resolver = new DefaultDependencyResolver();

        final Server server = new Server(resolver, null);
    }
}
