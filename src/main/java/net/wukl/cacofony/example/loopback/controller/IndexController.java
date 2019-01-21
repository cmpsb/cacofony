package net.wukl.cacofony.example.loopback.controller;

import net.wukl.cacofony.controller.Controller;
import net.wukl.cacofony.http.request.Request;
import net.wukl.cacofony.http.response.Response;
import net.wukl.cacofony.http.response.TextResponse;
import net.wukl.cacofony.route.Route;

/**
 * @author Luc Everse
 */
public class IndexController extends Controller {
    @Route(path = "/")
    public Response indexAction(final Request request) {
        return new TextResponse("Hello loopback!");
    }
}
