package net.cmpsb.cacofony.templating.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import net.cmpsb.cacofony.http.exception.InternalServerException;
import net.cmpsb.cacofony.http.response.Response;
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
     * Creates a new FreeMarker templating service.
     *
     * @param configuration the FreeMarker configuration to use
     */
    public FreeMarkerTemplatingService(final Configuration configuration) {
        this.configuration = configuration;
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
            this.configuration.setRecognizeStandardFileExtensions(true);
            final Template template = this.configuration.getTemplate(name);

            return new FreeMarkerResponse(template, values);
        } catch (final IOException ex) {
            throw new InternalServerException(ex);
        }
    }
}
