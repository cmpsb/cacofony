package net.cmpsb.cacofony.server;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

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

        assertThat("The set will claim it contains the port already.",
                   set.contains(shrdlu),
                   is(true));
    }
}
