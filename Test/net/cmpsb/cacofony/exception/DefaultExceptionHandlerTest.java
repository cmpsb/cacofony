package net.cmpsb.cacofony.exception;

import net.cmpsb.cacofony.http.exception.HttpException;
import net.cmpsb.cacofony.http.response.Response;
import net.cmpsb.cacofony.http.response.ResponseCode;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the default exception handler.
 *
 * @author Luc Everse
 */
public class DefaultExceptionHandlerTest {
    private DefaultExceptionHandler handler;

    @Before
    public void before() {
        this.handler = new DefaultExceptionHandler();
    }

    @Test
    public void testHttpExceptionWithoutMessage() {
        final HttpException exception = new HttpException(ResponseCode.BAD_GATEWAY);
        exception.addHeader("Exception-Test", "Exception Test");
        final Response response = this.handler.handle(null, exception);

        assertThat("The response code is correct.",
                   response.getStatus(),
                   is(equalTo(ResponseCode.BAD_GATEWAY)));

        assertThat("The header has been copied.",
                   response.getHeaders().get("Exception-Test"),
                   is(equalTo(Collections.singletonList("Exception Test"))));
    }

    @Test
    public void testHttpExceptionWithMessage() {
        final HttpException exception = new HttpException(ResponseCode.BAD_GATEWAY, "bad!");
        exception.addHeader("Exception-Test", "Exception Test");
        final Response response = this.handler.handle(null, exception);

        assertThat("The response code is correct.",
                response.getStatus(),
                is(equalTo(ResponseCode.BAD_GATEWAY)));

        assertThat("The header has been copied.",
                response.getHeaders().get("Exception-Test"),
                is(equalTo(Collections.singletonList("Exception Test"))));
    }

    @Test
    public void testGenericExceptionWithMessage() {
        final Exception exception = new IllegalArgumentException("Bad argument!");
        final Response response = this.handler.handle(null, exception);

        assertThat("The response code is 500 Internal Server Error.",
                   response.getStatus(),
                   is(equalTo(ResponseCode.INTERNAL_SERVER_ERROR)));
    }

    @Test
    public void testGenericExceptionWithoutMessage() {
        final Exception exception = new IllegalArgumentException();
        final Response response = this.handler.handle(null, exception);

        assertThat("The response code is 500 Internal Server Error.",
                response.getStatus(),
                is(equalTo(ResponseCode.INTERNAL_SERVER_ERROR)));
    }
}
