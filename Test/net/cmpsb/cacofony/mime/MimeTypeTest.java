package net.cmpsb.cacofony.mime;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Luc Everse
 */
public class MimeTypeTest {
    @Test
    public void testEqualsSameValues() {
        final MimeType etaoin = new MimeType("text", "plain");
        final MimeType shrdlu = new MimeType("text", "plain");

        assertEquals(etaoin, shrdlu);
        assertEquals(shrdlu, etaoin);
    }

    @Test
    public void testEqualsSubWildcard() {
        final MimeType etaoin = new MimeType("image", "*");
        final MimeType shrdlu = new MimeType("image", "jpeg");

        assertEquals(etaoin, shrdlu);
        assertEquals(shrdlu, etaoin);
    }

    @Test
    public void testEqualsFullWildcard() {
        final MimeType etaoin = new MimeType("*", "*");
        final MimeType shrdlu = new MimeType("application", "octet-stream");

        assertEquals(etaoin, shrdlu);
        assertEquals(shrdlu, etaoin);
    }

    @Test
    public void testEqualsDifferentSubtype() {
        final MimeType etaoin = new MimeType("text", "plain");
        final MimeType shrdlu = new MimeType("text", "html");

        assertNotEquals(etaoin, shrdlu);
        assertNotEquals(shrdlu, etaoin);
    }

    @Test
    public void testEqualsDifferentMainType() {
        final MimeType etaoin = new MimeType("audio", "mpeg");
        final MimeType shrldu = new MimeType("video", "mpeg");

        assertNotEquals(etaoin, shrldu);
        assertNotEquals(shrldu, etaoin);
    }

    @Test
    public void testEqualsDifferentValues() {
        final MimeType etaoin = new MimeType("text", "html+xml");
        final MimeType shrdlu = new MimeType("application", "java");

        assertNotEquals(etaoin, shrdlu);
        assertNotEquals(shrdlu, etaoin);
    }

    @Test
    public void testEqualsWrongTypes() {
        final MimeType type = new MimeType("video", "vp8");

        assertNotEquals(type, "video/vp8");
    }

    @Test
    public void testToString() {
        final MimeType type = new MimeType("test", "string");
        type.getParameters().put("q", "0.2");

        assertEquals(type.toString(), "test/string; q=0.2");
    }
}
