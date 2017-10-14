package net.cmpsb.cacofony.route;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

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
