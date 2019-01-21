package net.wukl.cacofony.templating.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.wukl.cacofony.http.exception.InternalServerException;
import net.wukl.cacofony.http.response.Response;
import net.wukl.cacofony.mime.MimeGuesser;
import net.wukl.cacofony.mime.MimeParser;
import net.wukl.cacofony.mime.MimeType;
import net.wukl.cacofony.templating.TemplatingService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 * @author Luc Everse
 */
public class FreeMarkerService extends TemplatingService {
    /**
     * The FreeMarker configuration to use.
     */
    private final Configuration configuration;

    /**
     * The MIME parser to use.
     */
    private final MimeParser mimeParser;

    /**
     * The MIME guesser to use in case FreeMarker doesn't know.
     */
    private final MimeGuesser guesser;

    /**
     * Creates a new FreeMarker templating service.
     *
     * @param configuration the FreeMarker configuration to use
     * @param mimeParser    the MIME parser to use
     * @param mimeGuesser   the MIME guesser to use if FreeMarker doesn't know
     */
    public FreeMarkerService(final Configuration configuration,
                             final MimeParser mimeParser,
                             final MimeGuesser mimeGuesser) {
        this.configuration = configuration;
        this.mimeParser = mimeParser;
        this.guesser = mimeGuesser;
    }

    /**
     * Renders a template as a response.
     * <p>
     * The response may be of any type.
     *
     * @param name the name, path or ID of the template to render
     * @param values   any arguments for the template
     * @return a response containing the rendered template
     */
    @Override
    public Response render(final String name, final Map<String, ?> values) {
        try {
            final Template template = this.configuration.getTemplate(name);

            final Response response = new PrerenderedFreeMarkerResponse(template, values);

            // Determine the MIME type of the response.
            // In the best case, FreeMarker knows the type and we can use that blindly.
            // If not we pre-pre-render the template and let the MIME guesser do its job on it,
            // hopefully leading to a valid conclusion this time.
            final MimeType type;
            final String rawType = template.getOutputFormat().getMimeType();
            if (rawType != null) {
                type = this.mimeParser.parse(rawType);
            } else {
                try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                     OutputStreamWriter writer = new OutputStreamWriter(buffer)) {
                    template.process(values, writer);

                    final byte[] bytes = buffer.toByteArray();
                    final ByteArrayInputStream in = new ByteArrayInputStream(bytes);

                    type = this.guesser.guessRemote(in);
                } catch (final TemplateException e) {
                    throw new RuntimeException(e);
                }
            }

            type.getParameters().put("charset", template.getEncoding());
            response.setContentType(type);

            return response;
        } catch (final IOException ex) {
            throw new InternalServerException(ex);
        }
    }
}
