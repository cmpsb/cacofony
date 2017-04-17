package net.cmpsb.cacofony.http.response;

import net.cmpsb.cacofony.server.MutableServerSettings;
import net.cmpsb.cacofony.server.ServerProperties;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the response preparer.
 *
 * @author Luc Everse
 */
public class ResponsePreparerTest {
    private MutableServerSettings settings;
    private ResponsePreparer preparer;

    @Before
    public void before() {
        this.settings = new MutableServerSettings();
        final ServerProperties properties = new ServerProperties();
        this.preparer = new ResponsePreparer(this.settings, properties);
    }

    @Test
    public void testUnpreparableResponse() {
        final Response response = new EmptyResponse();
        response.setHeader("Content-Type", "test-type");
        response.setHeader("Date", "now");
        response.setHeader("Server", "unit test");

        this.preparer.prepare(null, response);

        assertThat("The content type is unchanged.",
                   response.getHeaders().get("Content-Type"),
                   is(equalTo(Collections.singletonList("test-type"))));

        assertThat("The date is unchanged.",
                   response.getHeaders().get("Date"),
                   is(equalTo(Collections.singletonList("now"))));

        assertThat("The server is unchanged.",
                   response.getHeaders().get("Server"),
                   is(equalTo(Collections.singletonList("unit test"))));
    }

    @Test
    public void testHiddenServer() {
        this.settings.setBroadcastServerVersion(false);

        final Response response = new EmptyResponse();

        this.preparer.prepare(null, response);

        assertThat("There is no Server header.",
                response.getHeaders().containsKey("Server"),
                is(false));
    }
}
