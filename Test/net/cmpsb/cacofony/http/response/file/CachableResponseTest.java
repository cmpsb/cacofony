package net.cmpsb.cacofony.http.response.file;

import net.cmpsb.cacofony.server.Server;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(response.getHeaders().get("ETag")).as("ETag headers")
                .isNotNull()
                .hasAtLeastOneElementOfType(String.class);
    }

    @Test
    public void testNoEtag() {
        final ResourceResponse response = new ResourceResponse(Server.class, RES, Long.MAX_VALUE);
        response.prepare(null);

        assertThat(response.getHeaders()).as("headers").doesNotContainKey("ETag");
    }

    @Test
    public void testMaxAge() {
        final ResourceResponse response = new ResourceResponse(Server.class, RES, 1133);
        response.setMaxAge(600);
        response.prepare(null);

        assertThat(response.getHeaders().get("Cache-Control")).as("Cache-Control header")
            .isEqualTo(Collections.singletonList("max-age=600"));

        assertThat(response.getHeaders()).as("headers").containsKey("Expires");
    }

    @Test
    public void testNoMaxAge() {
        final ResourceResponse response = new ResourceResponse(Server.class, RES, 1133);
        response.setMaxAge(0);
        response.prepare(null);

        assertThat(response.getHeaders().get("Cache-Control")).as("Cache-Control header")
            .hasSize(1)
            .element(0).asString().contains("no-cache");
    }
}
