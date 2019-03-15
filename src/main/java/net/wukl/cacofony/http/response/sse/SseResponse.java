package net.wukl.cacofony.http.response.sse;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * A response sending server-sent events.
 *
 * This is the lambda variant of {@link AbstractSseResponse}.
 */
public final class SseResponse extends AbstractSseResponse {
    /**
     * The event generator to call.
     */
    private final EventGenerator generator;

    /**
     * Creates a new server-sent events response.
     *
     * @param charset the character set the response should be encoded with
     * @param generator the generator to call to generate server-sent events
     */
    public SseResponse(final Charset charset, final EventGenerator generator) {
        super(charset);
        this.generator = generator;
    }

    /**
     * Creates a new server-sent events response.
     *
     * @param generator the generator to call to generate server-sent events
     */
    public SseResponse(final EventGenerator generator) {
        this.generator = generator;
    }

    /**
     * Generates events until the function returns.
     *
     * @param sse the event consumer where events should be sent to
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void generate(final Sse sse) throws IOException {
        this.generator.generate(sse);
    }

    /**
     * A function generating server-sent events.
     */
    @FunctionalInterface
    public interface EventGenerator {
        /**
         * Generates events.
         *
         * @param sse the server-sent event consumer
         *
         * @throws IOException if an I/O error occurs
         */
        void generate(Sse sse) throws IOException;
    }
}
