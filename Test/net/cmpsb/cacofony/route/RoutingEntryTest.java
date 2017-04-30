package net.cmpsb.cacofony.route;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

/**
 * Tests for routing entries.
 *
 * @author Luc Everse
 */
public class RoutingEntryTest {
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(RoutingEntry.class).verify();
    }
}
