package net.cmpsb.cacofony.route;

import net.cmpsb.cacofony.di.DependencyResolver;
import net.cmpsb.cacofony.http.request.MutableRequest;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.http.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * The part of the router that takes care of passing the correct parameters to an action.
 *
 * @author Luc Everse
 */
public class ActionInvoker {
    private static final Logger logger = LoggerFactory.getLogger(ActionInvoker.class);

    /**
     * The dependency resolver creating this invoker.
     */
    private final DependencyResolver di;

    /**
     * A mapping between types and their functions to parse strings to that type.
     */
    private final Map<Class<?>, Function<String, Object>> primitiveCastMap;

    /**
     * Creates a new action invoker.
     *
     * @param di the dependency resolver for this invoker
     */
    public ActionInvoker(final DependencyResolver di) {
        this.di = di;

        this.primitiveCastMap = new HashMap<>();
        this.primitiveCastMap.put(String.class, s -> s);
        this.primitiveCastMap.put(byte.class, Byte::parseByte);
        this.primitiveCastMap.put(short.class, Short::parseShort);
        this.primitiveCastMap.put(int.class, Integer::parseInt);
        this.primitiveCastMap.put(long.class, Long::parseLong);
        this.primitiveCastMap.put(float.class, Float::parseFloat);
        this.primitiveCastMap.put(double.class, Double::parseDouble);
    }

    /**
     * Invokes the action that corresponds to a request under the given conditions.
     *
     * @param entry   the routing entry to invoke
     * @param request the request to invoke the entry with
     *
     * @return the entry's response
     *
     * @throws Exception any exception thrown by the route
     */
    public Response invoke(final RoutingEntry entry, final MutableRequest request)
            throws Exception {
        final Method method = entry.getMethod();
        final Object[] arguments = new Object[method.getParameterCount()];

        int pathParameterIndex = 0;
        for (int i = 0; i < arguments.length; ++i) {
            final Parameter param = method.getParameters()[i];

            if (this.isRequestParameter(param)) {
                arguments[i] = this.getRequestParameter(entry, param, request, pathParameterIndex);
                ++pathParameterIndex;
                continue;
            }

            final Class<?> type = param.getType();
            if (type == Request.class) {
                arguments[i] = request;
            } else if (type == RoutingEntry.class) {
                arguments[i] = entry;
            } else {
                arguments[i] = this.di.get(type);
            }
        }

        return (Response) entry.invoke(arguments);
    }

    /**
     * Extracts a request parameter from the request based on the name and type.
     *
     * @param entry the routing entry the request is for
     * @param parameter the action parameter
     * @param request the request to examine
     * @param index the current index of the next path parameter to extract
     *
     * @return the value for that parameter
     */
    private Object getRequestParameter(final RoutingEntry entry,
                                       final Parameter parameter,
                                       final Request request,
                                       final int index) {
        if (entry.getPath().getParameters().size() <= index) {
            return null;
        }

        final String name = entry.getPath().getParameters().get(index);
        final String value = request.getPathParameter(name);

        return this.parseToParameter(parameter, value);
    }

    /**
     * Decides whether or not a parameter should be filled with a request parameter.
     *
     * @param parameter the parameter to examine
     *
     * @return {@code true} if the parameter is a primitive or String, otherwise {@code false}
     */
    private boolean isRequestParameter(final Parameter parameter) {
        return parameter.getType().isPrimitive()
            || parameter.getType() == String.class;
    }

    /**
     * Parses a value from a string so that it can be applied to the given parameter.
     *
     * @param parameter the parameter that should be filled
     * @param string the value to fill it with
     *
     * @return the casted and/or parsed value
     */
    private Object parseToParameter(final Parameter parameter, final String string) {
        if (string == null) {
            return null;
        }

        final Class type = parameter.getType();

        if (!this.primitiveCastMap.containsKey(type)) {
            throw new ClassCastException();
        }

        return this.primitiveCastMap.get(type).apply(string);
    }
}
