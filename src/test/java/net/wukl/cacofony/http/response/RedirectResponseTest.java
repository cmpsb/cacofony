package net.wukl.cacofony.http.response;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the redirect response.
 *
 * @author Luc Everse
 */
public class RedirectResponseTest {
    @Test
    public void test() {
        final RedirectResponse response = new RedirectResponse("/page/home");
        response.prepare(null);

        assertThat(response.getHeaders()).as("headers")
                .containsEntry("Location", Collections.singletonList("/page/home"));

        assertThat(response.getStatus()).as("status").isEqualTo(ResponseCode.FOUND);
    }
}
