package net.cmpsb.cacofony.templating.freemarker;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.cmpsb.cacofony.http.exception.InternalServerException;
import net.cmpsb.cacofony.http.response.Response;
import net.cmpsb.cacofony.mime.MimeType;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

/**
 * @author Luc Everse
 */
public class StreamedFreeMarkerResponse extends Response {
    /**
     * The FreeMarker template to render.
     */
    private final Template template;

    /**
     * The FreeMarker data model to apply.
     */
    private final Map<String, ?> values;

    /**
     * Creates a new FreeMarker response.
     *
     * @param template the template to render
     * @param values   the data model to apply
     */
    public StreamedFreeMarkerResponse(final Template template, final Map<String, ?> values) {
        this.template = template;
        this.values = values;
        this.setContentType(MimeType.html());
    }

    /**
     * Writes the response body to the output stream.
     *
     * @param out the client's output stream
     * @throws IOException if an I/O error occurs during writing
     */
    @Override
    public void write(final OutputStream out) throws IOException {
        final Writer writer = new OutputStreamWriter(out);
        try {
            this.template.process(this.values, writer);
        } catch (final TemplateException e) {
            throw new InternalServerException(e);
        }
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
        return -1;
    }
}
