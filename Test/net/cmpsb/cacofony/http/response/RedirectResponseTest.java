package net.cmpsb.cacofony.http.response;

import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

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

        assertThat("The response has the Location header set.",
                   response.getHeaders().get("Location"),
                   is(equalTo(Collections.singletonList("/page/home"))));

        assertThat("The status is 302 Found.",
                   response.getStatus(),
                   is(equalTo(ResponseCode.FOUND)));
    }
}
