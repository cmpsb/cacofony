package net.cmpsb.cacofony.response;

/**
 * A base response object.
 *
 * @param <T> the type of the data the response contains
 *
 * @author Luc Everse
 */
public class Response<T> {
    /**
     * The HTTP return status code.
     */
    private int status = 200;

    /**
     * The content's MIME type.
     */
    private String mime = "text/html";

    /**
     * The response's data.
     */
    private T data;

    /**
     * Create a new, empty response.
     * The HTTP response code is set to 200 OK.
     */
    public Response() {
        this.data = null;
    }

    /**
     * Create a response with a HTTP response code.
     * The data is set to null.
     *
     * @param status the HTTP response code
     */
    public Response(final int status) {
        this.status = status;
    }

    /**
     * Create a response with some data set.
     * The HTTP response code is set to 200 OK.
     *
     * @param data the data to send
     */
    public Response(final T data) {
        this.data = data;
    }

    /**
     * Create a response with a HTTP response code and some data.
     *
     * @param status the HTTP response code
     * @param data   the data to send
     */
    public Response(final int status, final T data) {
        this.status = status;
        this.data = data;
    }

    /**
     * @return the HTTP response code
     */
    public int getStatus() {
        return this.status;
    }

    /**
     * @return the response's data
     */
    public T getData() {
        return this.data;
    }
}
