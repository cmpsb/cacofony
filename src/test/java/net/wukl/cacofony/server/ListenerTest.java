package net.wukl.cacofony.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Luc Everse
 */
public class ListenerTest {
    private ExecutorService executor;
    private ByteArrayInputStream in;
    private ByteArrayOutputStream out;
    private InetAddress address;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ConnectionHandler handler;

    @BeforeEach
    public void before() throws IOException {
        this.executor = mock(ExecutorService.class);
        when(this.executor.submit(any(Runnable.class))).thenAnswer(invocation -> {
            final Runnable runnable = invocation.getArgument(0);
            runnable.run();
            throw new RuntimeException();
        });

        this.out = new ByteArrayOutputStream();
        this.in = new ByteArrayInputStream(new byte[] {});
        this.address = InetAddress.getLoopbackAddress();

        this.clientSocket = mock(Socket.class);
        when(this.clientSocket.getInetAddress()).thenReturn(this.address);
        when(this.clientSocket.getPort()).thenReturn(0);
        when(this.clientSocket.getInputStream()).thenReturn(this.in);
        when(this.clientSocket.getOutputStream()).thenReturn(this.out);

        this.serverSocket = mock(ServerSocket.class);
        when(this.serverSocket.accept()).thenReturn(this.clientSocket);

        this.handler = mock(ConnectionHandler.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"http", "https"})
    public void testHandleCalled(final String scheme) throws IOException {
        doAnswer(invocation -> {
            throw new VerificationException();
        }).when(this.handler)
                .handle(eq(this.address), eq(0), eq(this.in), eq(this.out), eq(scheme));

        final Listener listener =
                new Listener(this.serverSocket, this.executor, this.handler, scheme);

        final Throwable t = assertThrows(Throwable.class, () ->
                assertTimeoutPreemptively(Duration.ofSeconds(1), listener::run)
        );

        assertThat(t).isInstanceOf(VerificationException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"http", "https"})
    public void testCloseCalled(final String scheme) throws IOException {
        doAnswer(invocation -> {
            throw new VerificationException();
        }).when(this.clientSocket).close();

        final Listener listener =
                new Listener(this.serverSocket, this.executor, this.handler, scheme);

        final Throwable t = assertThrows(Throwable.class, () ->
                assertTimeoutPreemptively(Duration.ofSeconds(1), listener::run)
        );

        assertThat(t).isInstanceOf(VerificationException.class);
    }

    private class VerificationException extends RuntimeException {
    }
}
