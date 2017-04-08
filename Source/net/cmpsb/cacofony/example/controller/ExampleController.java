package net.cmpsb.cacofony.example.controller;

import net.cmpsb.cacofony.controller.Controller;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.http.response.StreamedResponse;
import net.cmpsb.cacofony.http.response.TextResponse;
import net.cmpsb.cacofony.http.response.Response;
import net.cmpsb.cacofony.mime.MimeType;
import net.cmpsb.cacofony.route.Route;
import net.cmpsb.cacofony.util.Ob;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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
        return this.render("hello.ftlh", Ob.map(
                "name", request.getPathParameter("name", "you")
        ));
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
        final String filename = "/net/cmpsb/cacofony/example/plumbing.txt";
        final InputStream in = this.getClass().getResourceAsStream(filename);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        final String frameName = "/net/cmpsb/cacofony/example/plumbing.html";
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
}
