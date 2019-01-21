package net.wukl.cacofony.http.request;

import net.wukl.cacofony.http.cookie.CookieParser;
import net.wukl.cacofony.route.RoutingEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Matcher;

/**
 * Prepares requests before they're sent to the routing action.
 *
 * @author Luc Everse
 */
public class RequestPreparer {
    private static final Logger logger = LoggerFactory.getLogger(RequestPreparer.class);

    /**
     * The cookie parser to use.
     */
    private final CookieParser cookieParser;

    /**
     * The query string parser to use.
     */
    private final QueryStringParser queryStringParser;

    /**
     * Creates a new request preparer.
     *
     * @param cookieParser      the cookie parser to use
     * @param queryStringParser the query string parser to use
     */
    public RequestPreparer(final CookieParser cookieParser,
                           final QueryStringParser queryStringParser) {
        this.cookieParser = cookieParser;
        this.queryStringParser = queryStringParser;
    }

    /**
     * Prepares a request.
     *
     * @param request     the request to prepare
     * @param entry       the routing entry that matched the target
     * @param target      the target URL
     * @param pathMatcher the matcher that matched the request URL
     */
    public void prepare(final MutableRequest request,
                        final RoutingEntry entry,
                        final String target,
                        final Matcher pathMatcher) {
        // Parse the path parameters.
        final Map<String, String> params = entry.getPath().parseParameters(pathMatcher);
        request.setPathParameters(params);

        // Parse the query string.
        String path = target;
        String queryString = "";
        try {
            queryString = pathMatcher.group("QUERY");
            path = pathMatcher.group("PATH");
        } catch (final IllegalArgumentException ex) {
            logger.warn("Custom path regex for route {} does not include a "
                            + "PATH and/or QUERY group!",
                    entry.getName(), ex);
        }

        if (queryString == null) {
            queryString = "";
        }

        request.setPath(path, queryString);

        final Map<String, String> queryParams = this.queryStringParser.parse(queryString);
        request.setQueryParameters(queryParams);

        // Parse the cookies.
        request.setCookies(this.cookieParser.parseSimple(request.getHeader("Cookie")));
    }
}
