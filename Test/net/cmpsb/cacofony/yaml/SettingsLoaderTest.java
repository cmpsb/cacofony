package net.cmpsb.cacofony.yaml;

import net.cmpsb.cacofony.http.encoding.TransferEncoding;
import net.cmpsb.cacofony.server.DefaultSettings;
import net.cmpsb.cacofony.server.Port;
import net.cmpsb.cacofony.server.ServerSettings;
import net.cmpsb.cacofony.util.Ob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Luc Everse
 */
public class SettingsLoaderTest {
    private SettingsLoader loader;
    private DefaultSettings defaults;

    @BeforeEach
    public void before() {
        this.loader = new SettingsLoader();
        this.defaults = new DefaultSettings();
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    public void testEnableDisableCompression(final boolean status) {
        final Map<String, Object> spec = Ob.map(
            "compression enabled", status
        );

        final ServerSettings settings = this.loader.load(spec);

        assertThat(settings.isCompressionEnabled()).isEqualTo(status);
    }

    @Test
    public void testDefaultCompression() {
        final Map<String, Object> spec = Ob.map();

        final ServerSettings settings = this.loader.load(spec);

        assertThat(settings.isCompressionEnabled()).isEqualTo(this.defaults.isCompressionEnabled());
    }

    @Test
    public void testInvalidCompression() {
        final Map<String, Object> spec = Ob.map(
            "compression enabled", "when pigs fly"
        );

        assertThrows(InvalidYamlException.class, () -> this.loader.load(spec));
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    public void testEnableDisableDefaultCompression(final boolean status) {
        final Map<String, Object> spec = Ob.map(
                "compress by default", status
        );

        final ServerSettings settings = this.loader.load(spec);

        assertThat(settings.canCompressByDefault()).isEqualTo(status);
    }

    @Test
    public void testDefaultDefaultCompression() {
        final Map<String, Object> spec = Ob.map();

        final ServerSettings settings = this.loader.load(spec);

        assertThat(settings.canCompressByDefault()).isEqualTo(this.defaults.canCompressByDefault());
    }

    @Test
    public void testInvalidDefaultCompression() {
        final Map<String, Object> spec = Ob.map(
            "compress by default", "whatever floats your boat"
        );

        assertThrows(InvalidYamlException.class, () -> this.loader.load(spec));
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    public void testEnableDisableBroadcastInfo(final boolean status) {
        final Map<String, Object> spec = Ob.map(
                "broadcast server version", status
        );

        final ServerSettings settings = this.loader.load(spec);

        assertThat(settings.mayBroadcastServerVersion()).isEqualTo(status);
    }

    @Test
    public void testDefaultBroadcastInfo() {
        final Map<String, Object> spec = Ob.map();

        final ServerSettings settings = this.loader.load(spec);

        assertThat(settings.mayBroadcastServerVersion())
                .isEqualTo(this.defaults.mayBroadcastServerVersion());
    }

    @Test
    public void testInvalidBroadcastInfo() {
        final Map<String, Object> spec = Ob.map(
            "broadcast server version", "why are you asking me?"
        );

        assertThrows(InvalidYamlException.class, () -> this.loader.load(spec));
    }

    @Test
    public void testDirectPorts() {
        final Map<String, Object> spec = Ob.map(
            "ports", Arrays.asList(
                8080,
                8443
            )
        );

        final ServerSettings settings = this.loader.load(spec);

        assertThat(settings.getPorts())
                .containsExactly(new Port(8080, false), new Port(8443, true));
    }

    @Test
    public void testExpandedPorts() {
        final Map<String, Object> spec = Ob.map(
            "ports", Arrays.asList(
                Ob.map(
                    "port", 80,
                    "secure", true
                ),
                Ob.map(
                    "port", 443,
                    "secure", false
                )
            )
        );

        final ServerSettings settings = this.loader.load(spec);

        assertThat(settings.getPorts()).containsExactly(new Port(80, true), new Port(443, false));
    }

    @Test
    public void testMixedPorts() {
        final Map<String, Object> spec = Ob.map(
            "ports", Arrays.asList(
                22,
                Ob.map(
                    "port", 24,
                    "secure", true
                )
            )
        );

        final ServerSettings settings = this.loader.load(spec);

        assertThat(settings.getPorts()).containsExactly(new Port(22, true), new Port(24, true));
    }

    @Test
    public void testNoPorts() {
        final Map<String, Object> spec = Ob.map(
            "ports", Collections.emptyList()
        );

        final ServerSettings settings = this.loader.load(spec);

        assertThat(settings.getPorts()).isEmpty();
    }

    @Test
    public void testMissingPorts() {
        final Map<String, Object> spec = Ob.map();

        final ServerSettings settings = this.loader.load(spec);

        assertThat(settings.getPorts()).isEmpty();
    }

    @Test
    public void testInvalidDirectPort() {
        final Map<String, Object> spec = Ob.map(
            "ports", Arrays.asList(
                80,
                "four four three"
            )
        );

        assertThrows(InvalidYamlException.class, () -> this.loader.load(spec));
    }

    @Test
    public void testCompressionAlgorithms() {
        final Map<String, Object> spec = Ob.map(
                "algorithms", Collections.singletonList("deflate")
        );

        final ServerSettings settings = this.loader.load(spec);

        assertThat(settings.getCompressionAlgorithms()).containsExactly(TransferEncoding.DEFLATE);
    }

    @Test
    public void testDefaultAlgorithms() {
        final Map<String, Object> spec = Ob.map();

        final ServerSettings settings = this.loader.load(spec);

        assertThat(settings.getCompressionAlgorithms())
                .isEqualTo(this.defaults.getCompressionAlgorithms());
    }

    @Test
    public void testInvalidAlgorithm() {
        final Map<String, Object> spec = Ob.map(
                "algorithms", Arrays.asList("gzip", "press it all together")
        );

        assertThrows(InvalidYamlException.class, () -> this.loader.load(spec));
    }
}
