package net.wukl.cacofony.http.response.sse;

import net.wukl.cacofony.http.response.Response;
import net.wukl.cacofony.mime.MimeType;
import net.wukl.cacofony.util.CheckedExceptionTunnel;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A response sending server-sent events.
 *
 * An implementation must override the {@link #generate(Sse)} method, which can provide events
 * to the {@link Sse} event consumer instance while the method is active. Once this method returns,
 * the event stream will be closed.
 *
 * @see <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html#server-sent-events">
 *     HTML Standard Section 9.2: Server-sent events</a>
 */
public abstract class AbstractSseResponse extends Response {
    /**
     * The MIME type of an SSE response.
     */
    private static final MimeType MIME_UTF8 = new MimeType(
            "text", "event-stream"
    );

    static {
        MIME_UTF8.getParameters().put("charset", "utf-8");
    }

    /**
     * The character set the response should be encoded with.
     */
    private final Charset charset;

    /**
     * The MIME type of the response.
     */
    private final MimeType type;

    /**
     * Creates a new server-sent event response.
     *
     * @param charset the character set the response should be encoded with
     */
    public AbstractSseResponse(final Charset charset) {
        this.charset = charset;
        this.type = new MimeType(MIME_UTF8);
        this.type.getParameters().put("charset", this.charset.name().toLowerCase());
    }

    /**
     * Creates a new server-sent event response.
     *
     * The charset is set to UTF-8.
     */
    public AbstractSseResponse() {
        this.charset = StandardCharsets.UTF_8;
        this.type = MIME_UTF8;
    }

    /**
     * Writes the response body to the output stream.
     *
     * @param out the client's output stream
     *
     * @throws IOException if an I/O error occurs during writing
     */
    @Override
    public void write(final OutputStream out) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, this.charset));

        this.generate(ev -> {
            this.writeField(writer, "id", ev.getId());
            this.writeField(writer, "event", ev.getEventType());
            this.writeField(writer, "data", ev.getPayload());
            writer.write('\n');
            writer.flush();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getContentLength() {
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MimeType getContentType() {
        final var mime = new MimeType("text", "event-stream");
        mime.getParameters().put("charset", this.charset.displayName());
        return mime;
    }

    /**
     * Generates events until the function returns.
     *
     * @param sse the event consumer where events should be sent to
     *
     * @throws IOException if an I/O error occurs
     */
    public abstract void generate(Sse sse) throws IOException;

    /**
     * Writes a field of a server-sent response.
     *
     * If the value is {@code null}, no field is written at all.
     *
     * If the value contains multiple lines, the field is split up into multiple lines, each
     * beginning with the name of the field.
     *
     * @param writer the writer to write the event field to
     * @param field the name of the field
     * @param value the value
     *
     * @throws IOException if an I/O error occurs
     */
    private void writeField(
            final Writer writer, final String field, final String value
    ) throws IOException {
        if (value == null) {
            return;
        }

        try {
            value.lines().forEach(l -> {
                try {
                    writer.write(field);
                    writer.write(": ");
                    writer.write(l);
                    writer.write('\n');
                } catch (final IOException ex) {
                    throw new CheckedExceptionTunnel(ex);
                }
            });
        } catch (final CheckedExceptionTunnel ex) {
            // Yuck
            if (ex.getCause() instanceof IOException) {
                throw (IOException) ex.getCause();
            }

            throw ex;
        }
    }
}
