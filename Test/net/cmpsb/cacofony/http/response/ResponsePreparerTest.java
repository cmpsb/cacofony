package net.cmpsb.cacofony.http.response;

import net.cmpsb.cacofony.http.cookie.CookieWriter;
import net.cmpsb.cacofony.server.MutableServerSettings;
import net.cmpsb.cacofony.server.ServerProperties;
import net.cmpsb.cacofony.util.UrlCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the response preparer.
 *
 * @author Luc Everse
 */
public class ResponsePreparerTest {
    private MutableServerSettings settings;
    private ResponsePreparer preparer;

    @BeforeEach
    public void before() {
        this.settings = new MutableServerSettings();
        final ServerProperties properties = new ServerProperties();
        final UrlCodec urlCodec = new UrlCodec();
        final CookieWriter cookieWriter = new CookieWriter(urlCodec);
        this.preparer = new ResponsePreparer(this.settings, properties, cookieWriter);
    }

    @Test
    public void testUnpreparableResponse() {
        final Response response = new EmptyResponse();
        response.setHeader("Content-Type", "test-type");
        response.setHeader("Date", "now");
        response.setHeader("Server", "unit test");

        this.preparer.prepare(null, response);

        assertThat(response.getHeaders()).as("headers")
                .containsEntry("Content-Type", Collections.singletonList("test-type"))
                .containsEntry("Date", Collections.singletonList("now"))
                .containsEntry("Server", Collections.singletonList("unit test"));
    }

    @Test
    public void testHiddenServer() {
        this.settings.setBroadcastServerVersion(false);

        final Response response = new EmptyResponse();

        this.preparer.prepare(null, response);

        assertThat(response.getHeaders()).as("headers").doesNotContainKey("Server");
    }
}
