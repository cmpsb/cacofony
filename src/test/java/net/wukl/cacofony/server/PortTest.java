package net.wukl.cacofony.server;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Luc Everse
 */
public class PortTest {
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(Port.class).suppress(Warning.ALL_FIELDS_SHOULD_BE_USED).verify();
    }

    @Test
    public void testConflictingPorts() {
        final Port etaoin = new Port(80, true);
        final Port shrdlu = new Port(80, false);

        assertEquals(etaoin, shrdlu);

        final Set<Port> set = new HashSet<>();
        set.add(etaoin);

        Assertions.assertThat(set).contains(shrdlu);
    }
}
