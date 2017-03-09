package net.cmpsb.cacofony.routing;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

/**
 * Tests for the routing entry model class.
 *
 * @author Luc Everse
 */
public class RoutingEntryTest {
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(RoutingEntry.class).verify();
    }
}
