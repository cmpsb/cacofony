package net.wukl.cacofony.example.localhost.controller;

import net.wukl.cacofony.controller.Controller;
import net.wukl.cacofony.http.cookie.Cookie;
import net.wukl.cacofony.http.request.FormParser;
import net.wukl.cacofony.http.request.Method;
import net.wukl.cacofony.http.request.Request;
import net.wukl.cacofony.http.response.StreamedResponse;
import net.wukl.cacofony.http.response.TextResponse;
import net.wukl.cacofony.http.response.Response;
import net.wukl.cacofony.mime.MimeType;
import net.wukl.cacofony.route.Route;
import net.wukl.cacofony.util.Ob;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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
        final boolean flip = request.hasQueryParameter("flip");

        final StringBuilder builder = new StringBuilder("<!DOCTYPE html><html><head>");

        if (flip) {
            builder.append("<style>html {transform: rotate(180deg);}</style>");
        }

        builder.append("<meta charset='UTF-8'><title>Hello World</title></head>"
                + "<body><h1>Hello World!</h1></body>"
                + "</html>");

        final TextResponse response = new TextResponse(builder.toString());
        response.setContentType(MimeType.html());

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
        final Cookie visitCookie;
        if (request.getCookie("visitnr") != null) {
            visitCookie = request.getCookie("visitnr");
        } else {
            visitCookie = new Cookie("visitnr", "1");
        }

        int visit;
        try {
            visit = Integer.parseInt(visitCookie.getValue());
        } catch (final NumberFormatException ex) {
            visit = 1;
        }

        final Response response = this.render("hello.ftlh", Ob.map(
                "name", request.getPathParameter("name", "you"),
                "visitnr", visit
        ));

        final Cookie cookie = new Cookie("visitnr", String.valueOf(visit + 1));
        cookie.setPath(request.getUri());
        response.addCookie(cookie);

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
        return this.render("ua.ftlh", Ob.map(
            "ua", request.getHeader("User-Agent")
        ));
    }

    /**
     * Streams a book on plumbing.
     *
     * @param request the request
     *
     * @return a response
     */
    @Route(path = "/stream")
    public Response stream(final Request request) {
        final String filename = "/net/wukl/cacofony/example/localhost/plumbing.txt";
        final InputStream in = this.getClass().getResourceAsStream(filename);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        final String frameName = "/net/wukl/cacofony/example/localhost/plumbing.html";
        final InputStream frameIn = this.getClass().getResourceAsStream(frameName);
        final BufferedReader frameReader = new BufferedReader(new InputStreamReader(frameIn));

        final Response response = new StreamedResponse(out -> {
            while (true) {
                final String line = frameReader.readLine();
                if (line == null) {
                    break;
                }

                out.write(line.getBytes(StandardCharsets.UTF_8));
            }
            out.flush();

            while (true) {
                final String line = reader.readLine();

                if (line == null) {
                    break;
                }

                out.write(line.getBytes(StandardCharsets.UTF_8));
                out.write('\n');
                out.flush();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // Do nothing.
                }
            }
        });

        response.setContentType(MimeType.html());

        return response;
    }

    /**
     * Displays a form.
     *
     * @param request the request
     *
     * @return the response
     */
    @Route(path = "/form", methods = {Method.GET})
    public Response formAction(final Request request) {
        return this.render("form.ftlh");
    }

    @Route(path = "/form", methods = {Method.POST})
    public Response formPostAction(final Request request) throws IOException {
        final String bodyStr = request.readFullBody(Integer.MAX_VALUE, StandardCharsets.UTF_8);

        final FormParser parser = new FormParser();
        final Map<String, List<String>> form = parser.parse(bodyStr);

        final TextResponse response = new TextResponse();

        for (final String key : form.keySet()) {
            response.append(key).append(":\n");

            for (final String value : form.get(key)) {
                response.append("      ").append(value).append("\n");
            }
        }

        return response;
    }
}
