package net.wukl.cacofony.http.response.sse;

import java.io.IOException;

/**
 * The server-sent event consumer.
 */
@FunctionalInterface
public interface Sse {
    /**
     * Sends the server-sent event to the client.
     *
     * @param ev the event to send
     *
     * @throws IOException if an I/O error occurs
     */
    void send(ServerSentEvent ev) throws IOException;
}
