package net.cmpsb.cacofony.controller;

import net.cmpsb.cacofony.http.request.Method;
import net.cmpsb.cacofony.mime.MimeParser;
import net.cmpsb.cacofony.mime.MimeType;
import net.cmpsb.cacofony.route.CompiledPath;
import net.cmpsb.cacofony.route.PathCompiler;
import net.cmpsb.cacofony.route.Requirement;
import net.cmpsb.cacofony.route.Route;
import net.cmpsb.cacofony.route.Router;
import net.cmpsb.cacofony.route.RoutingEntry;
import net.wukl.cacodi.DependencyResolver;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A loader for annotated controllers.
 * <p>
 * This class will automatically find a set of controllers and add each annotated route
 * to the router.
 *
 * @author Luc Everse
 */
public class ControllerLoader {
    private static final Logger logger = LoggerFactory.getLogger(ControllerLoader.class);

    /**
     * The dependency resolver to use.
     */
    private final DependencyResolver dependencyResolver;

    /**
     * The route to store the routes in.
     */
    private final Router router;

    /**
     * The MIME parser to use.
     */
    private final MimeParser mimeParser;

    /**
     * The path compiler to use.
     */
    private final PathCompiler pathCompiler;

    /**
     * Create a new controller loader.
     *
     * @param dependencyResolver the dependency resolver to use
     * @param router             the router responsible for handling requests
     * @param parser             the MIME parser to use
     * @param pathCompiler       the path compiler to use
     */
    public ControllerLoader(final DependencyResolver dependencyResolver,
                            final Router router,
                            final MimeParser parser,
                            final PathCompiler pathCompiler) {
        this.dependencyResolver = dependencyResolver;
        this.router = router;
        this.mimeParser = parser;
        this.pathCompiler = pathCompiler;
    }

    /**
     * Load all known controllers through reflection.
     *
     * @param prefix the path prefix the controllers fall under
     * @param pack   the package to inspect for controllers
     */
    public void loadAll(final String prefix, final String pack) {
        Reflections reflections = new Reflections(pack);
        Set<Class<? extends Controller>> controllers = reflections.getSubTypesOf(Controller.class);

        controllers.forEach(c -> this.load(prefix, c));
    }

    /**
     * Load a controller's routes.
     *
     * @param prefix the path prefix the controllers fall under
     * @param type   the controller type
     * @param <T>    the controller type
     */
    public <T extends Controller> void load(final String prefix, final Class<T> type) {
        final Controller controller = this.dependencyResolver.get(type);

        for (final java.lang.reflect.Method method : type.getMethods()) {
            for (Route annotation : method.getAnnotationsByType(Route.class)) {
                this.mapSingle(prefix, controller, method, annotation);
            }
        }
    }

    /**
     * Load a single route.
     * An action may have multiple routes, this loads only one.
     *
     * @param prefix the path prefix the controllers fall under
     * @param controller the controller instance
     * @param method the action itself
     * @param route  the one annotation to map
     */
    private void mapSingle(final String prefix,
                           final Controller controller,
                           final java.lang.reflect.Method method,
                           final Route route) {
        final Map<String, String> requirements = new HashMap<>();
        for (final Requirement requirement : route.requirements()) {
            requirements.put(requirement.name(), requirement.regex());
        }

        final CompiledPath path = this.pathCompiler.compile(prefix + route.path(), requirements);

        final List<MimeType> types = Arrays.stream(route.types())
            .map(this.mimeParser::parse)
            .collect(Collectors.toList());

        final List<Method> methods = Arrays.asList(route.methods());

        final String name;
        if (!route.name().isEmpty()) {
            name = route.name();
        } else {
            final String canonicalControllerName = controller.getClass().getCanonicalName();
            final String controllerName;
            if (canonicalControllerName != null) {
                controllerName = canonicalControllerName;
            } else {
                controllerName = controller.getClass().getName();
            }

            name = controllerName + ":/" + route.path();
        }

        final RoutingEntry entry = new RoutingEntry(name, path, controller, method, methods, types);

        this.router.addRoute(entry);
    }
}
