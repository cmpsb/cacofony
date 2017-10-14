package net.cmpsb.cacofony.exception;

import net.cmpsb.cacofony.http.exception.HttpException;
import net.cmpsb.cacofony.http.response.Response;
import net.cmpsb.cacofony.http.response.ResponseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the default exception handler.
 *
 * @author Luc Everse
 */
public class DefaultExceptionHandlerTest {
    private DefaultExceptionHandler handler;

    @BeforeEach
    public void before() {
        this.handler = new DefaultExceptionHandler();
    }

    @Test
    public void testHttpExceptionWithoutMessage() {
        final HttpException exception = new HttpException(ResponseCode.BAD_GATEWAY);
        exception.addHeader("Exception-Test", "Exception Test");
        final Response response = this.handler.handle(null, exception);

        assertThat(response.getStatus()).as("status").isEqualTo(ResponseCode.BAD_GATEWAY);
        assertThat(response.getHeaders()).as("adopted headers")
                .containsEntry("Exception-Test", Collections.singletonList("Exception Test"));
    }

    @Test
    public void testHttpExceptionWithMessage() {
        final HttpException exception = new HttpException(ResponseCode.BAD_GATEWAY, "bad!");
        exception.addHeader("Exception-Test", "Exception Test");
        final Response response = this.handler.handle(null, exception);

        assertThat(response.getStatus()).as("status").isEqualTo(ResponseCode.BAD_GATEWAY);
        assertThat(response.getHeaders()).as("adopted headers")
                .containsEntry("Exception-Test", Collections.singletonList("Exception Test"));
    }

    @Test
    public void testGenericExceptionWithMessage() {
        final Exception exception = new IllegalArgumentException("Bad argument!");
        final Response response = this.handler.handle(null, exception);

        assertThat(response.getStatus()).as("status").isEqualTo(ResponseCode.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testGenericExceptionWithoutMessage() {
        final Exception exception = new IllegalArgumentException();
        final Response response = this.handler.handle(null, exception);

        assertThat(response.getStatus()).as("status").isEqualTo(ResponseCode.INTERNAL_SERVER_ERROR);
    }
}
