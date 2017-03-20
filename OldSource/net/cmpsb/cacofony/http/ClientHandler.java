package net.cmpsb.cacofony.http;

import java.net.Socket;

/**
 * The handler for incoming connections.
 *
 * @author Luc Everse
 */
public class ClientHandler implements Runnable {
    /**
     * The client socket.
     */
    private final Socket client;

    private final RequestParser parser;

    /**
     * Create a new client handler.
     *
     * @param socket the socket the client is communicating through
     */
    public ClientHandler(final Socket socket) {
        this.client = socket;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

    }
}
