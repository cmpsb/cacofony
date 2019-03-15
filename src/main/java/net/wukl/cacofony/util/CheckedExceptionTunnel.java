package net.wukl.cacofony.util;

/**
 * An unchecked exception used to tunnel unhandleable checked exceptions out of a function.
 */
public class CheckedExceptionTunnel extends RuntimeException {
    /**
     * Creates a new checked exception tunnel.
     *
     * @param cause the exception to tunnel
     */
    public CheckedExceptionTunnel(final Throwable cause) {
        super(cause);
    }
}
