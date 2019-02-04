package net.wukl.cacofony.server;

import net.wukl.cacofony.http2.Http2ProtocolFactory;
import net.wukl.cacofony.server.protocol.HttpProtocolFactory;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The default listener factory.
 *
 * @author Luc Everse
 */
public class DefaultListenerFactory implements ListenerFactory {
    /**
     * The server socket factory to use.
     */
    private final SSLServerSocketFactory factory;

    /**
     * The connection handler to use.
     */
    private final ConnectionHandler handler;

    /**
     * The thread pool to run the requests in.
     */
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * The factory for HTTP/1 protocol instances.
     */
    private final HttpProtocolFactory httpProtocolFactory;

    /**
     * The factory for HTTP/2 protocol instances.
     */
    private final Http2ProtocolFactory http2ProtocolFactory;

    /**
     * Creates a new default listener factory.
     *
     * @param factory  the SSL server socket factory
     * @param handler  the connection handler to direct the listeners to
     * @param httpProtocolFactory  the factory for HTTP protocol instances
     * @param http2ProtocolFactory the factory for HTTP/2 protocol instances
     */
    public DefaultListenerFactory(
            final SSLServerSocketFactory factory,
            final ConnectionHandler handler,
            final HttpProtocolFactory httpProtocolFactory,
            final Http2ProtocolFactory http2ProtocolFactory
    ) {
        this.factory = factory;
        this.handler = handler;
        this.httpProtocolFactory = httpProtocolFactory;
        this.http2ProtocolFactory = http2ProtocolFactory;
    }

    /**
     * Builds a listener listening on a port.
     *
     * @param port the port
     *
     * @return the listener
     *
     * @throws IOException if an I/O error occurs
     */
    public Listener build(final Port port) throws IOException {
        if (port.isSecure()) {
            return this.bootSecure(port);
        } else {
            return this.bootInsecure(port);
        }
    }

    /**
     * Boots an insecure listener.
     *
     * @param port the port the listener should watch
     *
     * @return the listener
     *
     * @throws IOException if an I/O error occurs
     */
    private Listener bootInsecure(final Port port) throws IOException {
        final ServerSocket socket = new ServerSocket(port.getPort());
        return new InsecureListener(
                socket, this.executor, this.handler, "http", this.httpProtocolFactory
        );
    }

    /**
     * Boots a secure listener.
     *
     * @param port the port the listener should watch
     *
     * @return the listener
     *
     * @throws IOException if an I/O error occurs
     */
    private Listener bootSecure(final Port port) throws IOException {
        final var socket = (SSLServerSocket) this.factory.createServerSocket(port.getPort());
        return new SecureListener(
                socket, this.executor, this.handler, "https",
                this.httpProtocolFactory, this.http2ProtocolFactory
        );
    }
}
