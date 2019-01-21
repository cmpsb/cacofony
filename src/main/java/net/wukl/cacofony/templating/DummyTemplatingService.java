package net.wukl.cacofony.templating;

import net.wukl.cacofony.http.response.Response;
import net.wukl.cacofony.http.response.TextResponse;

import java.util.Map;

/**
 * A templating service that tells the user that no templating service has been installed.
 *
 * @author Luc Everse
 */
public class DummyTemplatingService extends TemplatingService {
    /**
     * Renders a template as a response.
     * <p>
     * The response may be of any type.
     *
     * @param template the name, path or ID of the template to render
     * @param values   any arguments for the template
     * @return a response containing the rendered template
     */
    @Override
    public Response render(final String template, final Map<String, ?> values) {
        return new TextResponse("No templating service active.");
    }
}
