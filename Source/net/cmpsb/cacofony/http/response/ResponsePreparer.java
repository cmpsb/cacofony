package net.cmpsb.cacofony.http.response;

import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.mime.MimeType;
import net.cmpsb.cacofony.server.ServerProperties;
import net.cmpsb.cacofony.server.ServerSettings;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Prepares a response.
 * <p>
 * First, it lets a response prepare itself through the {@link Response#prepare(Request)} method.
 * Then it will apply some global logic before sending it to the client.
 *
 * @author Luc Everse
 */
public class ResponsePreparer {
    /**
     * The configuration for the current server.
     */
    private final ServerSettings settings;

    /**
     * The static server properties.
     */
    private final ServerProperties properties;

    /**
     * Creates a new response parser.
     *
     * @param settings   the server configuration
     * @param properties the static server properties
     */
    public ResponsePreparer(final ServerSettings settings,
                            final ServerProperties properties) {
        this.settings = settings;
        this.properties = properties;
    }

    /**
     * Prepares a response.
     *
     * @param request  the request that triggered this response
     * @param response the response to prepare
     */
    public void prepare(final Request request, final Response response) {
        response.prepare(request);

        final Map<String, List<String>> headers = response.getHeaders();

        // Set the content type header if it's missing.
        if (!headers.containsKey("Content-Type")) {

            // If the response has no content type set, default to application/octet-stream.
            final MimeType contentType;
            if (response.getContentType() != null) {
                contentType = response.getContentType();
            } else {
                contentType = MimeType.octetStream();
            }

            response.setHeader("Content-Type", contentType.toString());
        }

        // Add on a Date header.
        if (!headers.containsKey("Date")) {
            final ZonedDateTime datetime = ZonedDateTime.now(ZoneId.of("GMT"));
            final String dateLine = DateTimeFormatter.RFC_1123_DATE_TIME.format(datetime);

            response.setHeader("Date", dateLine);
        }

        if (!headers.containsKey("Server") && this.settings.mayBroadcastServerVersion()) {
            final String version = this.properties.getProperty("net.cmpsb.cacofony.version");
            if (version != null) {
                response.setHeader("Server", "Cacofony/" + version);
            } else {
                response.setHeader("Server", "Cacofony/with love from your IDE");
            }
        }
    }
}
