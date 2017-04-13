package net.cmpsb.cacofony.server;

/**
 * A TCP port to bind to.
 *
 * @author Luc Everse
 */
public final class Port {
    /**
     * The actual numeric port.
     */
    private final int port;

    /**
     * Whether to enable HTTPS or not.
     */
    private final boolean secure;

    /**
     * Creates a new port.
     *
     * @param port   the numeric value
     * @param secure whether to enable HTTPS
     */
    public Port(final int port, final boolean secure) {
        this.port = port;
        this.secure = secure;
    }

    /**
     * Returns the port's numeric value.
     *
     * @return the numeric value
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Returns whether the connection uses TLS.
     *
     * @return {@code true} if the port uses TLS, otherwise {@code false}
     */
    public boolean isSecure() {
        return this.secure;
    }

    /**
     * Checks whether this object is equal to another.
     *
     * @param obj the other object
     *
     * @return true if the ports are equal, false otherwise
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Port)) {
            return false;
        }

        final Port other = (Port) obj;

        return this.port == other.port;
    }

    /**
     * Calculates the object's hash code.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(this.port);
    }
}
