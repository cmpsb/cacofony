package net.cmpsb.cacofony.util;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author Luc Everse
 */
public class ObITest {
    private ObI ob;

    @Before
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

        assertThat("The map is as expected.",
                   map,
                   is(equalTo(expected)));
    }

    @Test
    public void testEmptyMap() {
        final Map<String, String> map = this.ob.map();

        assertThat("The map is empty.",
                   map.isEmpty(),
                   is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOddMapEntries() {
        this.ob.map("one", "first", "two");
    }

    @Test
    public void testValidSet() {
        final Set<String> set = this.ob.set("wan", "too", "tree");

        final Set<String> expected = new HashSet<>();
        expected.add("wan");
        expected.add("too");
        expected.add("tree");

        assertThat("The set is as expected.",
                   set,
                   is(equalTo(expected)));
    }

    @Test
    public void testEmptySet() {
        final Set<String> set = this.ob.set();

        assertThat("The set is empty.",
                   set,
                   is(emptyCollectionOf(String.class)));
    }

    @Test
    public void testMultiEqualsForEqualValues() {
        final boolean equals = this.ob.multiEquals(
            "one", "one",
            3, 3
        );

        assertThat("The values are equal.",
                   equals,
                   is(true));
    }

    @Test
    public void testMultiEqualsForInequalValues() {
        final boolean equals = this.ob.multiEquals(
            1, 1,
            "one", "two",
            2, 2
        );

        assertThat("The values are not equal.",
                   equals,
                   is(false));
    }

    @Test
    public void testMultiEqualsNoParameters() {
        final boolean equals = this.ob.multiEquals();

        assertThat("The empty set is considered equal.",
                   equals,
                   is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiEqualsOddEntries() {
        this.ob.multiEquals("one", "two", "three");
    }
}
