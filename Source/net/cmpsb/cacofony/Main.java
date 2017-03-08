package net.cmpsb.cacofony;

/**
 * The server's default entry point.
 *
 * @author Luc Everse
 */
public class Main {

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
        Server server = new Server();
    }
}
