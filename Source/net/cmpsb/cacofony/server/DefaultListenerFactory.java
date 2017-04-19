package net.cmpsb.cacofony.server;

import javax.net.ServerSocketFactory;
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
    private final ServerSocketFactory factory;

    /**
     * The connection handler to use.
     */
    private final ConnectionHandler handler;

    /**
     * The thread pool to run the requests in.
     */
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Creates a new default listener factory.
     *
     * @param factory  the SSL server socket factory
     * @param handler  the connection handler to direct the listeners to
     */
    public DefaultListenerFactory(final ServerSocketFactory factory,
                                  final ConnectionHandler handler) {
        this.factory = factory;
        this.handler = handler;
    }

    /**
     * Boots a listener listening on a port.
     *
     * @param port the port
     *
     * @throws IOException if an I/O error occurs
     */
    public void boot(final Port port) throws IOException {
        if (port.isSecure()) {
            this.bootSecure(port);
        } else {
            this.bootInsecure(port);
        }
    }

    /**
     * Boots an insecure listener.
     *
     * @param port the port the listener should watch
     *
     * @throws IOException if an I/O error occurs
     */
    private void bootInsecure(final Port port) throws IOException {
        final ServerSocket socket = new ServerSocket(port.getPort());
        final Listener listener = new Listener(socket, this.executor, this.handler, "http");

        final Thread runner = new Thread(listener);
        runner.start();
    }

    /**
     * Boots a secure listener.
     *
     * @param port the port the listener should watch
     *
     * @throws IOException if an I/O error occurs
     */
    private void bootSecure(final Port port) throws IOException {
        final ServerSocket socket = this.factory.createServerSocket(port.getPort());

        final Listener listener = new Listener(socket, this.executor, this.handler, "https");

        final Thread runner = new Thread(listener);
        runner.start();
    }
}
