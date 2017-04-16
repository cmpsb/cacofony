package net.cmpsb.cacofony.server;

import net.cmpsb.cacofony.di.DefaultDependencyResolver;
import net.cmpsb.cacofony.di.DependencyResolver;
import net.cmpsb.cacofony.mime.FastMimeParser;
import net.cmpsb.cacofony.mime.MimeDb;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

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

    @Before
    public void before() {
        this.resolver = new DefaultDependencyResolver();
        this.settings = new MutableServerSettings();

        this.resolver.add(new MimeDb());
        this.resolver.add(new FastMimeParser());

        this.expectedPorts = new HashSet<>();

        this.expectedPorts.add(new Port(80, false));
        this.expectedPorts.add(new Port(443, true));

        this.factory = new VerifyingListenerFactory(this.expectedPorts);
        this.resolver.add(this.factory, ListenerFactory.class);
    }

    @Test
    public void testRunDefaultPort() throws IOException {
        final Server server = new Server(this.resolver, this.settings);
        server.start();

        this.factory.verify();
    }

    @Test
    public void testRunExplicitPort() throws IOException {
        this.settings.addInsecurePort(8080);
        this.expectedPorts.clear();
        this.expectedPorts.addAll(this.settings.getPorts());

        assertThat("Adding one port does actually add exactly one port.",
                   this.expectedPorts.size(),
                   is(1));

        final Server server = new Server(this.resolver, this.settings);
        server.start();

        this.factory.verify();
    }

    @Test(expected = RunningServerException.class)
    public void testRunAlreadyRunning() throws IOException {
        final Server server = new Server(this.resolver, this.settings);
        server.start();

        this.expectedPorts.add(new Port(80, false));
        this.expectedPorts.add(new Port(443, true));
        server.start();
    }

    @Test(expected = RunningServerException.class)
    public void testRegisterAlreadyRunning() throws IOException {
        final Server server = new Server(this.resolver, this.settings);
        server.start();

        server.register(String.class, "fake dependency");
    }

    @Test(expected = RunningServerException.class)
    public void testAddStaticFilesAlreadyRunning() throws IOException {
        final Server server = new Server(this.resolver, this.settings);
        server.start();

        server.addStaticFiles("/static/", "/");
    }

    @Test(expected = RunningServerException.class)
    public void testScanPackageAlreadyRunning() throws IOException {
        final Server server = new Server(this.resolver, this.settings);
        server.start();

        server.scanPackage("net.cmpsb.cacofony");
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
                fail("Not all ports have been booted.");
            }
        }
    }
}