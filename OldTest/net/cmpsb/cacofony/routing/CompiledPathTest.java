package net.cmpsb.cacofony.routing;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

/**
 * Tests for the compiled path container class.
 *
 * @author Luc Everse
 */
public class CompiledPathTest {
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(CompiledPath.class).verify();
    }
}
