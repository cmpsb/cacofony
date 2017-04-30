package net.cmpsb.cacofony.server;

import net.cmpsb.cacofony.di.DependencyResolver;
import net.cmpsb.cacofony.mime.FastMimeParser;
import net.cmpsb.cacofony.mime.MimeParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the server.
 *
 * @author Luc Everse
 */
public class ServerTest {
    private DependencyResolver resolver;
    private MutableServerSettings settings;
    private Set<Port> expectedPorts;
    private VerifyingListenerFactory factory;

    @BeforeEach
    public void before() {
        this.resolver = new DependencyResolver();
        this.settings = new MutableServerSettings();

        this.resolver.implement(MimeParser.class, FastMimeParser.class);

        this.expectedPorts = new HashSet<>();

        this.expectedPorts.add(new Port(80, false));
        this.expectedPorts.add(new Port(443, true));

        this.factory = new VerifyingListenerFactory(this.expectedPorts);
        this.resolver.add(ListenerFactory.class, this.factory);
    }

    @Test
    public void testRunDefaultPort() throws IOException {
        final Server server = new ServerBuilder(this.resolver).build();
        server.run();

        this.factory.verify();
    }

    @Test
    public void testRunExplicitPort() throws IOException {
        this.settings.addInsecurePort(8080);
        this.expectedPorts.clear();
        this.expectedPorts.addAll(this.settings.getPorts());

        assertThat(this.expectedPorts).hasSize(1);

        final ServerBuilder builder = new ServerBuilder(this.resolver);
        builder.setSettings(this.settings);
        final Server server = builder.build();
        server.run();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            // Do nothing.
        }

        this.factory.verify();
    }

    @Test
    public void testRunAlreadyRunning() throws IOException {
        final Server server = new ServerBuilder(this.resolver).build();
        server.run();

        this.expectedPorts.add(new Port(80, false));
        this.expectedPorts.add(new Port(443, true));
        assertThrows(RunningServerException.class, () -> server.run());
    }

    private class VerifyingListenerFactory implements ListenerFactory {
        final Set<Port> expectedPorts;

        VerifyingListenerFactory(final Set<Port> expectedPorts) {
            this.expectedPorts = expectedPorts;
        }

        /**
         * Boots a listener listening on a port.
         *
         * @param port the port
         * @throws IOException if an I/O error occurs
         */
        @Override
        public void boot(final Port port) throws IOException {
            if (!this.expectedPorts.contains(port)) {
                fail("Unexpected port " + port);
            }

            this.expectedPorts.remove(port);
        }

        public void verify() {
            if (!this.expectedPorts.isEmpty()) {
                fail("Not all ports have been booted: " + this.expectedPorts);
            }
        }
    }
}
