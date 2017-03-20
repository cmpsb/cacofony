package net.cmpsb.cacofony.response;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * A response transformer that takes the string form of the object and writes it, plainly,
 * to the output stream.
 *
 * @author Luc Everse
 */
public class RawResponseTransformer implements ResponseTransformer {
    /**
     * Transform a response.
     *
     * @param object the response
     * @param writer where to write the response
     */
    @Override
    public void transform(final Object object, final PrintWriter writer) {
        writer.append(String.valueOf(object));
    }

    /**
     * Transform a complex response.
     *
     * @param response the complex response
     * @param writer   where to write the response
     */
    @Override
    public void transform(final Response response, final PrintWriter writer) {
        this.transform(response.getData(), writer);
    }

    /**
     * @return the content types this response transformer may produce
     */
    @Override
    public List<String> getContentTypes() {
        return Arrays.asList("text/plain", "*/*");
    }
}
