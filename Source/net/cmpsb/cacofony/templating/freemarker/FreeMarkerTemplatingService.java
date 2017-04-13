package net.cmpsb.cacofony.templating.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import net.cmpsb.cacofony.di.Inject;
import net.cmpsb.cacofony.http.exception.InternalServerException;
import net.cmpsb.cacofony.http.response.Response;
import net.cmpsb.cacofony.mime.MimeParser;
import net.cmpsb.cacofony.mime.MimeType;
import net.cmpsb.cacofony.templating.TemplatingService;

import java.io.IOException;
import java.util.Map;

/**
 * @author Luc Everse
 */
public class FreeMarkerTemplatingService extends TemplatingService {
    /**
     * The FreeMarker configuration to use.
     */
    private final Configuration configuration;

    /**
     * The MIME parser to use.
     */
    private final MimeParser mimeParser;

    /**
     * Creates a new FreeMarker templating service.
     *
     * @param configuration the FreeMarker configuration to use
     * @param mimeParser    the MIME parser to use
     */
    public FreeMarkerTemplatingService(@Inject("arg: configuration")
                                       final Configuration configuration,
                                       final MimeParser mimeParser) {
        this.configuration = configuration;
        this.mimeParser = mimeParser;
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

            final MimeType type = this.mimeParser.parse(template.getOutputFormat().getMimeType());
            type.getParameters().put("charset", template.getEncoding());
            response.setContentType(type);

            return response;
        } catch (final IOException ex) {
            throw new InternalServerException(ex);
        }
    }
}
