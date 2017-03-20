package net.cmpsb.cacofony.http;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the request wrapper.
 *
 * @author Luc Everse
 */
public class RequestTest {
    private Map<String, String> routeParameters;
    private Map<String, String[]> httpParameters;

    private MockHttpServletRequest rawRequest;
    private Request request;

    @Before
    public void before() {
        this.routeParameters = new HashMap<>();
        this.routeParameters.put("one", "1");
        this.routeParameters.put("two", "two");
        this.routeParameters.put("three", "333");
        this.routeParameters.put("empty", "");

        this.httpParameters = new HashMap<>();
        this.httpParameters.put("one", new String[]{"1", "one"});
        this.httpParameters.put("two", new String[]{"two", "2"});
        this.httpParameters.put("three", new String[]{"333"});
        this.httpParameters.put("empty", new String[]{""});
        this.httpParameters.put("none", new String[]{});

        this.rawRequest = new MockHttpServletRequest();
        this.rawRequest.setParameters(this.httpParameters);

        this.request = new Request(this.rawRequest, this.routeParameters);
    }

    @Test
    public void testGetRawRequest() {
        assertThat("The raw request is preserved.",
                   this.request.getRawRequest(),
                   is(equalTo(this.rawRequest)));
    }

    @Test
    public void testGetMethod() {

    }
}
