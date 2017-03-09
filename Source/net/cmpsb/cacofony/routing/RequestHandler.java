package net.cmpsb.cacofony.routing;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The default handler for incoming HTTP requests.
 *
 * @author Luc Everse
 */
public class RequestHandler extends AbstractHandler {

    /**
     * Handle an incoming HTTP request.
     *
     * @param target      the path to match
     * @param baseRequest the raw Jetty request
     * @param request     the Jetty request, possibly wrapped
     * @param response    the Jetty response\
     *
     * @throws IOException      if an I/O exception occurs while writing the request
     * @throws ServletException a general exception when handling a request
     */
    @Override
    public void handle(final String target,
                       final Request baseRequest,
                       final HttpServletRequest request,
                       final HttpServletResponse response)
            throws IOException, ServletException {

    }
}
