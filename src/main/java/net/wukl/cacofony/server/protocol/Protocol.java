package net.wukl.cacofony.server.protocol;

/**
 * A protocol supported by the server.
 */
public interface Protocol {
    /**
     * Returns the name of the protocol.
     *
     * @return the name
     */
    String getName();

    /**
     * Handles an active connection.
     *
     * The protocol should stay active until the connection can be closed.
     *
     * @return the next protocol to use for the connection
     *
     * @throws Throwable if any error occurs
     */
    Protocol handle() throws Throwable;
}
