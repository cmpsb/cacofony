package net.wukl.cacofony.server.host;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Luc Everse
 */
public class TestControllerPackage {
    @Test
    public void testConstructor() {
        final ControllerPackage controllerPackage = new ControllerPackage("etaoin", "shrdlu");

        assertThat(controllerPackage.getPack()).as("package").isEqualTo("etaoin");
        assertThat(controllerPackage.getPrefix()).as("prefix").isEqualTo("shrdlu");
    }
}
