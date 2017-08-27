package net.cmpsb.cacofony.route;

import net.cmpsb.cacofony.controller.Controller;
import net.cmpsb.cacofony.http.request.Request;
import net.cmpsb.cacofony.http.response.Response;
import net.cmpsb.cacofony.http.response.TextResponse;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/**
 * Tests for routing entries.
 *
 * @author Luc Everse
 */
public class RoutingEntryTest {
    private Controller etaoin;
    private Controller shrdlu;

    private Method etaoinMethod;
    private Method shrdluMethod;

    @BeforeEach
    public void before() {
        this.etaoin = new EtaoinController();
        this.shrdlu = new ShrdluController();

        try {
            this.etaoinMethod = EtaoinController.class.getMethod("etaoinAction", Request.class);
            this.shrdluMethod = ShrdluController.class.getMethod("shrdluAction", Request.class);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testEquals() {
        EqualsVerifier
                .forClass(RoutingEntry.class)
                .withPrefabValues(Controller.class, this.etaoin, this.shrdlu)
                .withPrefabValues(Method.class, this.etaoinMethod, this.shrdluMethod)
                .verify();
    }

    private class EtaoinController extends Controller {
        public Response etaoinAction(final Request request) {
            return new TextResponse("etaoin");
        }
    }

    private class ShrdluController extends Controller {
        public Response shrdluAction(final Request request) {
            return new TextResponse("shrdlu");
        }

    }
}
