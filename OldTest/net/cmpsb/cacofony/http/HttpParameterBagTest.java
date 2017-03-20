package net.cmpsb.cacofony.http;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the HTTP parameter bag.
 *
 * @author Luc Everse
 */
public class HttpParameterBagTest extends ParameterBagTest<HttpParameterBag> {
    @Before
    public void before() {
        super.before();

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameters(this.requiredParameters);

        this.bag = new HttpParameterBag(request);
    }

    @Test
    public void testGetAll() {
        final List<String> values = this.bag.getAll("one");

        assertThat("Retrieving all values lists all possible values.",
                values,
                is(equalTo(Arrays.asList("1", "one"))));
    }
}
