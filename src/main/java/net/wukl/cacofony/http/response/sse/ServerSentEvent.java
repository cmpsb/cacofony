package net.wukl.cacofony.http.response.sse;

/**
 * A server-sent event.
 *
 * @see <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events">
 *     HTML Standard Section 9.2: Server-sent events</a>
 */
public class ServerSentEvent {
    /**
     * The identifier of the event.
     */
    private final String id;

    /**
     * The type of the event.
     */
    private final String eventType;

    /**
     * The data of the event.
     */
    private final String payload;

    /**
     * Creates a new server-sent event.
     *
     * @param id the identifier of the event
     * @param eventType the type of the event
     * @param payload the data of the event
     */
    public ServerSentEvent(final String id, final String eventType, final String payload) {
        this.id = id;
        this.eventType = eventType;
        this.payload = payload;
    }

    /**
     * Creates a new server-sent event without an id.
     *
     * @param eventType the type of the event
     * @param payload the data of the event
     */
    public ServerSentEvent(final String eventType, final String payload) {
        this(null, eventType, payload);
    }

    /**
     * Creates a new server-sent event without an id nor type.
     *
     * @param payload the data of the event
     */
    public ServerSentEvent(final String payload) {
        this(null, null, payload);
    }

    /**
     * Returns the identifier of the event.
     *
     * If this is {@code null}, the identifier is omitted from the event.
     *
     * @return the identifier or {@code null} if there is none
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the type of the event.
     *
     * If this is {@code null}, the type is omitted from the event.
     *
     * @return the type or {@code null} if there is none
     */
    public String getEventType() {
        return this.eventType;
    }

    /**
     * Returns the data of the event.
     *
     * If this is {@code null}, the data is omitted from the event.
     *
     * @return the data or {@code null} if there is none
     */
    public String getPayload() {
        return this.payload;
    }
}
