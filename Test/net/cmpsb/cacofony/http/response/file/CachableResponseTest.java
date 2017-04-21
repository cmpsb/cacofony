package net.cmpsb.cacofony.http.response.file;

import net.cmpsb.cacofony.server.Server;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

/**
 * Tests for cachable responses.
 *
 * This suite uses {@link ResourceResponse} instances because its constructor allows for implicit
 * modification dates.
 *
 * @author Luc Everse
 */
public class CachableResponseTest {
    private static final String RES = "/net/cmpsb/cacofony/mime.types";

    @Test
    public void testEtag() {
        final ResourceResponse response = new ResourceResponse(Server.class, RES, 1133);
        response.prepare(null);

        assertThat("There is an ETag header.",
                   response.getHeaders().get("ETag").size(),
                   is(greaterThanOrEqualTo(1)));
    }

    @Test
    public void testNoEtag() {
        final ResourceResponse response = new ResourceResponse(Server.class, RES, Long.MAX_VALUE);
        response.prepare(null);

        assertThat("There is no ETag header.",
                   response.getHeaders().containsKey("ETag"),
                   is(false));
    }

    @Test
    public void testMaxAge() {
        final ResourceResponse response = new ResourceResponse(Server.class, RES, 1133);
        response.setMaxAge(600);
        response.prepare(null);

        assertThat("There is a Cache-Control header with the correct value.",
                   response.getHeaders().get("Cache-Control").get(0),
                   containsString("max-age=600"));

        assertThat("There is an Expires header.",
                   response.getHeaders().containsKey("Expires"),
                   is(true));
    }

    @Test
    public void testNoMaxAge() {
        final ResourceResponse response = new ResourceResponse(Server.class, RES, 1133);
        response.setMaxAge(0);
        response.prepare(null);

        assertThat("There is a Cache-Control header containing at least no-cache.",
                   response.getHeaders().get("Cache-Control").get(0),
                   containsString("no-cache"));

        assertThat("There is no Expires header.",
                   response.getHeaders().containsKey("Expires"),
                   is(false));
    }
}
