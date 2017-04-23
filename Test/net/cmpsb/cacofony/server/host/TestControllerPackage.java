package net.cmpsb.cacofony.server.host;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author Luc Everse
 */
public class TestControllerPackage {
    @Test
    public void testConstructor() {
        final ControllerPackage controllerPackage = new ControllerPackage("etaoin", "shrdlu");

        assertThat("The package is correct.",
                   controllerPackage.getPack(),
                   is(equalTo("etaoin")));

        assertThat("The prefix is correct.",
                   controllerPackage.getPrefix(),
                   is(equalTo("shrdlu")));
    }
}
