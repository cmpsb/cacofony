package net.cmpsb.cacofony.http.response;

import net.cmpsb.cacofony.http.request.Request;

/**
 * A response that redirects the client to another URL.
 *
 * @author Luc Everse
 */
public class RedirectResponse extends TextResponse {
    /**
     * The target URL.
     */
    private final String url;

    /**
     * Creates a new redirect response with a custom status code.
     *
     * @param code the status code
     * @param url  the URL to redirect to
     */
    public RedirectResponse(final ResponseCode code, final String url) {
        super(code);
        this.url = url;
        this.append(this.generateMessage());
    }

    /**
     * Creates a new redirect response.
     * <p>
     * The response code is 302 Found.
     *
     * @param url the URL to redirect to
     */
    public RedirectResponse(final String url) {
        this(ResponseCode.FOUND, url);
    }

    /**
     * Generates the HTML page indicating a redirect.
     *
     * @return the HTML page
     */
    private String generateMessage() {
        return "<!DOCTYPE><html lang='en'><head><meta charset'UTF-8'><title>You're being "
             + "redirected.</title></head><body><p>You're being redirected to <a href='"
             + this.url + "'>" + this.url + "</a></body></html>";
    }

    /**
     * Prepares the response.
     * <p>
     * This sets the Location header.
     *
     * @param request the request that triggered this response
     */
    @Override
    public void prepare(final Request request) {
        super.prepare(request);

        this.setHeader("Location", this.url);
    }
}
