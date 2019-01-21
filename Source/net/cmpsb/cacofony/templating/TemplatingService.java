package net.cmpsb.cacofony.templating;

import net.cmpsb.cacofony.http.response.Response;
import net.wukl.cacodi.Service;

import java.util.Collections;
import java.util.Map;

/**
 * @author Luc Everse
 */
public abstract class TemplatingService extends Service {
    /**
     * Renders a template as a response.
     * <p>
     * The response may be of any type.
     *
     * @param template the name, path or ID of the template to render
     * @param values   any arguments for the template
     *
     * @return a response containing the rendered template
     */
    public abstract Response render(String template, Map<String, ?> values);

    /**
     * Renders a template as a response.
     * <p>
     * The response may be of any type.
     *
     * @param template the name, path or ID of the template to render
     *
     * @return a response containing the rendered template
     */
    public Response render(final String template) {
        return this.render(template, Collections.emptyMap());
    }
}
