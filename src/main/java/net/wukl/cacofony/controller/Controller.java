package net.wukl.cacofony.controller;

import net.wukl.cacofony.http.response.Response;
import net.wukl.cacofony.templating.TemplatingService;
import net.wukl.cacodi.Inject;
import net.wukl.cacodi.Service;

import java.util.Map;

/**
 * @author Luc Everse
 */
public abstract class Controller extends Service {
    /**
     * The templating service to use.
     */
    @Inject
    private TemplatingService templatingService;

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
    public Response render(final String template, final Map<String, ?> values) {
        return this.templatingService.render(template, values);
    }

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
        return this.templatingService.render(template);
    }
}
