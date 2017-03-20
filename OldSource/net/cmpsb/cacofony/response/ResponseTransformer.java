package net.cmpsb.cacofony.response;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * A transformer to project a response onto an output writer.
 *
 * @author Luc Everse
 */
public interface ResponseTransformer {
    /**
     * Transform a response.
     *
     * @param object the response
     * @param writer where to write the response
     *
     * @throws IOException if an I/O exception occurs in the print writer
     */
    void transform(Object object, PrintWriter writer) throws IOException;

    /**
     * Transform a complex response.
     *
     * @param response the complex response
     * @param writer   where to write the response
     *
     * @throws IOException if an I/O exception occurs in the print writer
     */
    void transform(Response response, PrintWriter writer) throws IOException;

    /**
     * @return the content types this response transformer may produce
     */
    List<String> getContentTypes();
}
