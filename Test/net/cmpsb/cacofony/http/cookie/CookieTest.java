package net.cmpsb.cacofony.http.cookie;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;


/**
 * @author Luc Everse
 */
public class CookieTest {
    @Test
    public void testEquals() {
        EqualsVerifier.forClass(Cookie.class).verify();
    }
}
