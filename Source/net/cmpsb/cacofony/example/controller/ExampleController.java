package net.cmpsb.cacofony.example.controller;

import net.cmpsb.cacofony.controller.Controller;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.http.response.TextResponse;
import net.cmpsb.cacofony.http.response.Response;
import net.cmpsb.cacofony.mime.MimeType;
import net.cmpsb.cacofony.route.Route;
import net.cmpsb.cacofony.util.Ob;

/**
 * @author Luc Everse
 */
public class ExampleController extends Controller {
    /**
     * Serve an example index page.
     *
     * @param request the request
     *
     * @return a response
     */
    @Route(path = "/", types = {"text/plain"})
    @Route(path = "/helloworld")
    public Response indexAction(final Request request) {
        final TextResponse response = new TextResponse();
        response.setContent(
            "<html>"
          +   "<head><meta charset='UTF-8'><title>Hello World</title></head>"
          +   "<body><h1>Hello World!</h1></body>"
          + "</html>");

        response.setContentType(MimeType.html());
        response.getContentType().getParameters().put("charset", "UTF-8");

        return response;
    }

    /**
     * Says hello to the client.
     *
     * @param request the request
     *
     * @return a response
     */
    @Route(path = "/hello/{name}")
    @Route(path = "/hello/")
    public Response helloAction(final Request request) {
        final Response response = this.render("hello.ftlh", Ob.map(
                "name", request.getPathParameter("name", "you")
        ));

        response.setCompressionAllowed(true);
        response.setContentType(MimeType.html());
        response.getContentType().getParameters().put("charset", "UTF-8");

        return response;
    }

    /**
     * Spies on the client.
     *
     * @param request the request
     *
     * @return a response
     */
    @Route(path = "/ua")
    public Response userAgentAction(final Request request) {
        final Response response = this.render("ua.ftlh", Ob.map(
            "ua", request.getHeader("User-Agent")
        ));

        response.setCompressionAllowed(true);
        response.setContentType(MimeType.html());
        response.getContentType().getParameters().put("charset", "UTF-8");

        return response;
    }
}
