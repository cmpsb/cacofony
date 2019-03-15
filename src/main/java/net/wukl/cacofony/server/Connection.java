package net.wukl.cacofony.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

/**
 * A connection with a client.
 *
 * The class represents (and contains) a single TCP stream over which one or more request-response
 * cycles are performed.
 *
 * The connection is protocol-agnostic.
 */
public class Connection {
    /**
     * The address of the client.
     */
    private final InetAddress address;

    /**
     * The port the client opened the request on.
     */
    private final int port;

    /**
     * The stream into the server.
     */
    private final InputStream in;

    /**
     * The stream out of the server.
     */
    private final OutputStream out;

    /**
     * The URI scheme of the request.
     */
    private final String scheme;

    /**
     * Creates a new connection.
     *
     * @param address the address of the client
     * @param port the port the connection is on
     * @param in the stream into the server
     * @param out the stream out of the server
     * @param scheme the URI scheme of the request
     */
    public Connection(
            final InetAddress address, final int port,
            final InputStream in, final OutputStream out,
            final String scheme
    ) {
        this.address = address;
        this.port = port;
        this.in = in;
        this.out = out;
        this.scheme = scheme;
    }

    /**
     * Returns the address of the client.
     *
     * @return the address
     */
    public InetAddress getAddress() {
        return this.address;
    }

    /**
     * Returns the port the client used to connect to the server.
     *
     * @return the port
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Returns the input stream of the connection.
     *
     * @return the input stream
     */
    public InputStream getIn() {
        return this.in;
    }

    /**
     * Returns the output stream of the connection.
     *
     * @return the output stream
     */
    public OutputStream getOut() {
        return this.out;
    }

    /**
     * Returns the scheme of the URI used to connect with.
     *
     * @return the scheme
     */
    public String getScheme() {
        return this.scheme;
    }
}
