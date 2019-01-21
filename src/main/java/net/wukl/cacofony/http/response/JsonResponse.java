package net.wukl.cacofony.http.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * A response object returning JSON.
 *
 * @author Luc Everse
 */
public class JsonResponse extends Response {
    /**
     * The default Gson instances for new responses.
     */
    private static final Gson DEFAULT_GSON = new GsonBuilder().create();

    /**
     * The Gson instance to use.
     */
    private final Gson gson;

    /**
     * The data to send.
     */
    private final byte[] data;

    /**
     * Creates a new JSON response with a status code and a custom serializer.
     *
     * @param code the HTTP status code
     * @param data the data to send
     * @param gson the GSON instance to use
     */
    public JsonResponse(final ResponseCode code, final Object data, final Gson gson) {
        super(code);
        this.gson = gson;
        this.data = this.serialize(data);
    }

    /**
     * Creates a new JSON response with a status code.
     *
     * @param code the HTTP status code
     * @param data the data to send
     */
    public JsonResponse(final ResponseCode code, final Object data) {
        this(code, data, DEFAULT_GSON);
    }

    /**
     * Creates a new JSON response.
     * <p>
     * The status code is {@code 200 OK}.
     *
     * @param data the data to send
     */
    public JsonResponse(final Object data) {
        this(ResponseCode.OK, data, DEFAULT_GSON);
    }

    /**
     * Serializes the content data as a byte array.
     *
     * @param data the data to serialize
     *
     * @return the data as a byte array
     */
    private byte[] serialize(final Object data) {
        return this.gson.toJson(data).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Writes the response body to the client.
     * <p>
     * The response will be encoded as JSON using Gson.
     *
     * @param out the client's output stream
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(final OutputStream out) throws IOException {
        out.write(this.data);
    }

    /**
     * Calculates the length, in bytes, of the data to send.
     * <p>
     * If {@code -1}, then a collection of transfer encodings are applied. This allows for
     * big responses that don't fit entirely within memory.
     */
    @Override
    public long getContentLength() {
        return this.data.length;
    }
}
