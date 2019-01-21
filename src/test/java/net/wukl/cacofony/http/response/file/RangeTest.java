package net.wukl.cacofony.http.response.file;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

/**
 * Tests for the range object.
 *
 * @author Luc Everse
 */
public class RangeTest {
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(Range.class).verify();
    }
}
