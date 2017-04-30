package net.cmpsb.cacofony.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Luc Everse
 */
public class ObITest {
    private ObI ob;

    @BeforeEach
    public void before() {
        this.ob = new ObI();
    }

    @Test
    public void testValidMap() {
        final Map<String, String> map = this.ob.map(
            "one",   "first",
            "two",   "twice",
            "three", "3"
        );

        final Map<String, String> expected = new HashMap<>();
        expected.put("one",   "first");
        expected.put("two",   "twice");
        expected.put("three", "3");

        assertThat(map).isEqualTo(expected);
    }

    @Test
    public void testEmptyMap() {
        final Map<String, String> map = this.ob.map();

        assertThat(map).isEmpty();
    }

    @Test
    public void testOddMapEntries() {
        assertThrows(IllegalArgumentException.class, () -> this.ob.map("one", "first", "two"));
    }

    @Test
    public void testValidSet() {
        final Set<String> set = this.ob.set("wan", "too", "tree");

        final Set<String> expected = new HashSet<>();
        expected.add("wan");
        expected.add("too");
        expected.add("tree");

        assertThat(set).isEqualTo(expected);
    }

    @Test
    public void testEmptySet() {
        final Set<String> set = this.ob.set();

        assertThat(set).isEmpty();
    }

    @Test
    public void testMultiEqualsForEqualValues() {
        final boolean equals = this.ob.multiEquals(
            "one", "one",
            3, 3
        );

        assertThat(equals).isTrue();
    }

    @Test
    public void testMultiEqualsForInequalValues() {
        final boolean equals = this.ob.multiEquals(
            1, 1,
            "one", "two",
            2, 2
        );

        assertThat(equals).isFalse();
    }

    @Test
    public void testMultiEqualsNoParameters() {
        final boolean equals = this.ob.multiEquals();

        assertThat(equals).isTrue();
    }

    @Test
    public void testMultiEqualsOddEntries() {
        assertThrows(IllegalArgumentException.class, () ->
                this.ob.multiEquals("one", "two", "three")
        );
    }
}
