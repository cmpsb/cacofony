package net.cmpsb.cacofony.route;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Tests for the routing path compiler.
 *
 * @author Luc Everse
 */
public class PathCompilerTest {
    private PathCompiler compiler;
    private Map<String, String> requirements;

    @Before
    public void before() {
        this.compiler = new PathCompiler();
        this.requirements = new HashMap<>();
    }

    @Test
    public void testNormalPath() {
        final String path = "/about";

        final CompiledPath compiled = this.compiler.compile(path, this.requirements);

        final Matcher fullMatcher = compiled.getPattern().matcher("/about");
        final Matcher trailingSlashMatcher = compiled.getPattern().matcher("/about/");
        final Matcher queryMatcher = compiled.getPattern().matcher("/about/?foo=bar");

        assertThat("The pattern compiled with no parameters.",
                   compiled.getParameters().size(),
                   is(0));

        assertThat("The path itself matches.",
                   fullMatcher.matches(),
                   is(true));

        assertThat("The path with a trailing slash matches.",
                   trailingSlashMatcher.matches(),
                   is(true));

        assertThat("The path with a query string attached matches.",
                   queryMatcher.matches(),
                   is(true));
    }

    @Test
    public void testParameterPath() {
        final String path = "/api/items/{groupid}/{itemid}/";

        final CompiledPath compiled = this.compiler.compile(path, this.requirements);

        final Matcher shortSlashMatcher = compiled.getPattern().matcher("/api/items/");

        final Matcher fullMatcher =
                compiled.getPattern().matcher("/api/items/group1/item3?foo=bar");

        assertThat("The path compiled with two parameters.",
                   compiled.getParameters().size(),
                   is(2));

        assertThat("The short form does not match.",
                   shortSlashMatcher.matches(),
                   is(false));

        assertThat("The full form does match.",
                   fullMatcher.matches(),
                   is(true));

        final String groupId = fullMatcher.group("groupid");
        final String itemId  = fullMatcher.group("itemid");
        final String query   = fullMatcher.group("QUERY");

        assertThat("The first parameter is parsed correctly.",
                   groupId,
                   is(equalTo("group1")));

        assertThat("The second parameter is parsed correctly.",
                   itemId,
                   is(equalTo("item3")));

        assertThat("The query string is preserved.",
                   query,
                   is(equalTo("?foo=bar")));
    }

    @Test
    public void testParameterWithRequirementsPath() {
        final String path = "/api/items/{groupid}/{itemid}";
        this.requirements.put("groupid", "[A-Z]+");
        this.requirements.put("itemid", "\\d+");

        final CompiledPath compiled = this.compiler.compile(path, this.requirements);
        final Pattern pattern = compiled.getPattern();

        final Matcher correctMatcher = pattern.matcher("/api/items/GROUPONE/2233");
        final Matcher badGroupMatcher = pattern.matcher("/api/items/GROUP1/2233");
        final Matcher badItemMatcher  = pattern.matcher("/api/items/GROUPONE/twotwothreethree");

        assertThat("The path compiled with two parameters.",
                   compiled.getParameters().size(),
                   is(2));

        assertThat("A correct route matches the requirements.",
                   correctMatcher.matches(),
                   is(true));

        assertThat("A bad 'group name' fails to match.",
                   badGroupMatcher.matches(),
                   is(false));

        assertThat("A bad 'item id' fails to match.",
                   badItemMatcher.matches(),
                   is(false));
    }

    @Test(expected = BadRoutePathException.class)
    public void testUnterminatedParameterBlock() {
        final String path = "/api/items/{groupid/";

        this.compiler.compile(path, this.requirements);
    }

    @Test(expected = BadRoutePathException.class)
    public void testBadParameterName() {
        final String path = "/api/items/{item_id}";

        this.compiler.compile(path, this.requirements);
    }
}
