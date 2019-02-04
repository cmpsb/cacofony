package net.wukl.cacofony.server;

import net.wukl.cacofony.server.protocol.Http2ProtocolFactory;
import net.wukl.cacofony.server.protocol.HttpProtocolFactory;
import net.wukl.cacofony.server.protocol.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * A listener for incoming HTTPS requests.
 */
public class SecureListener implements Listener {

    private static final Logger logger = LoggerFactory.getLogger(SecureListener.class);

    /**
     * The socket to listen on.
     */
    private final SSLServerSocket socket;

    /**
     * The executor service (or thread pool) to run the handler in.
     */
    private final ExecutorService executor;

    /**
     * The connection handler to use.
     */
    private final ConnectionHandler handler;

    /**
     * The connection scheme this listener uses.
     */
    private final String scheme;

    /**
     * The factory for HTTP/1 protocol instances.
     */
    private final HttpProtocolFactory httpFactory;

    /**
     * The factory for HTTP/2 protocol instances.
     */
    private final Http2ProtocolFactory http2Factory;

    /**
     * Creates a new listener.
     *
     * @param socket   the socket to listen on
     * @param executor the thread pool for incoming requests
     * @param handler  the connection handler to use
     * @param scheme   the connection scheme this listener handles
     * @param httpProtocolFactory the factory for HTTP/1 protocol instances
     * @param http2ProtocolFactory the factory for HTTP/2 protocol instances
     */
    public SecureListener(
            final SSLServerSocket socket, final ExecutorService executor,
            final ConnectionHandler handler, final String scheme,
            final HttpProtocolFactory httpProtocolFactory,
            final Http2ProtocolFactory http2ProtocolFactory
    ) {
        this.socket = socket;
        this.executor = executor;
        this.handler = handler;
        this.scheme = scheme;
        this.httpFactory = httpProtocolFactory;
        this.http2Factory = http2ProtocolFactory;

        final var socketParams = this.socket.getSSLParameters();
        socketParams.setApplicationProtocols(
                new String[] {"h2", "http/1.1", "http/1.0", "http/0.9"}
        );
        this.socket.setSSLParameters(socketParams);
    }

    /**
     * Runs the listener on the secure socket.
     *
     * Each incoming connection is executed in the executor service.
     */
    @Override
    public void run() {
        logger.info("Now listening on port {}.", this.socket.getLocalPort());
        for (;;) {
            try {
                final var client = (SSLSocket) this.socket.accept();
                client.setSoTimeout(4444);

                client.startHandshake();
                this.waitForHandshakeCompletion(client, 15, 12);

                final var rawProtocolName = client.getApplicationProtocol();
                final String protocolName;
                if (rawProtocolName != null && !rawProtocolName.isEmpty()) {
                    protocolName = rawProtocolName;
                } else {
                    protocolName = "http/1.0";
                }

                final InetAddress address = client.getInetAddress();
                final int port = client.getPort();

                this.executor.submit(this.generateThread(client, address, port, protocolName));
            } catch (final IOException e) {
                logger.error("I/O exception while accepting a client: ", e);
            }
        }
    }

    /**
     * Sleeps until the connection has finished its handshake.
     *
     * @param client the socket the handshake is happening on
     * @param maxTries the maximum number of checks to see whether the handshake was completed
     * @param timeout the number of milliseconds to sleep between tries
     */
    private void waitForHandshakeCompletion(
            final SSLSocket client, final int maxTries, final int timeout
    ) {
        int numWaits = 0;
        while (client.getApplicationProtocol() == null && numWaits < maxTries) {
            try {
                Thread.sleep(timeout);
            } catch (final InterruptedException ex) {
                break;
            }
            ++numWaits;
        }
    }

    /**
     * Generates a thread running a single connection.
     *
     * @param client the client socket
     * @param address the address of the client
     * @param port the port the client connected on
     * @param protocolName the negotiated protocol
     *
     * @return the thread serving the client
     */
    private Runnable generateThread(
            final Socket client, final InetAddress address, final int port,
            final String protocolName
    ) {
        return () -> {
            try (InputStream in = client.getInputStream();
                 OutputStream out = client.getOutputStream()) {
                final var connection = new Connection(address, port, in, out, this.scheme);

                logger.debug("Negotiated protocol {}", protocolName);

                final Protocol protocol;
                switch (protocolName) {
                    case "http/0.9":
                    case "http/1.0":
                    case "http/1.1":
                        protocol = this.httpFactory.build(connection);
                        break;
                    case "h2":
                        protocol = this.http2Factory.build(connection);
                        break;
                    default:
                        throw new RuntimeException("TLS chose unknown ALP");
                }

                this.handler.handle(connection, protocol);
            } catch (final IOException ex) {
                logger.error("I/O exception while accepting a client: ", ex);
            } catch (final Throwable ex) {
                logger.error("Unhandled exception while accepting a client: ", ex);
            } finally {
                try {
                    client.close();
                } catch (final IOException ex) {
                    logger.error("I/O exception while closing socket: ", ex);
                }
            }
        };
    }
}
