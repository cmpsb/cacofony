package net.wukl.cacofony.templating.freemarker;

import freemarker.template.Template;
import net.wukl.cacofony.http.request.Request;
import net.wukl.cacofony.http.response.Response;
import net.wukl.cacofony.mime.MimeType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

/**
 * @author Luc Everse
 */
public class PrerenderedFreeMarkerResponse extends Response {
    /**
     * The FreeMarker template to render.
     */
    private final Template template;

    /**
     * The FreeMarker data model to apply.
     */
    private final Map<String, ?> values;

    /**
     * The prerendered contents of the response.
     */
    private byte[] contents;

    /**
     * Creates a new FreeMarker response.
     *
     * @param template the template to render
     * @param values   the data model to apply
     */
    public PrerenderedFreeMarkerResponse(final Template template, final Map<String, ?> values) {
        this.template = template;
        this.values = values;
        this.setContentType(MimeType.html());
    }

    /**
     * Prepares the request by prerendering the template to a buffer.
     *
     * @param request the request that triggered this response
     */
    @Override
    public void prepare(final Request request) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            final Writer writer = new OutputStreamWriter(out, this.template.getEncoding());
            this.template.process(this.values, writer);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }

        this.contents = out.toByteArray();
    }

    /**
     * Writes the response body to the output stream.
     *
     * @param out the client's output stream
     * @throws IOException if an I/O error occurs during writing
     */
    @Override
    public void write(final OutputStream out) throws IOException {
        out.write(this.contents);
    }

    /**
     * Calculates the length, in bytes, of the data to send.
     * <p>
     * If {@code -1}, then a collection of transfer encodings are applied. This allows for
     * big responses that don't fit entirely within memory.
     *
     * @return the length, in bytes, of the data to send or {@code -1} if it's unknown
     */
    @Override
    public long getContentLength() {
        return this.contents.length;
    }
}
