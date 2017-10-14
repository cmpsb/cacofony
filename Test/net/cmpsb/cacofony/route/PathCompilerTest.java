package net.cmpsb.cacofony.route;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the routing path compiler.
 *
 * @author Luc Everse
 */
public class PathCompilerTest {
    private PathCompiler compiler;
    private Map<String, String> requirements;

    @BeforeEach
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

        assertThat(compiled.getParameters()).as("parameters").isEmpty();
        assertThat(fullMatcher.matches()).as("full path matches").isTrue();
        assertThat(trailingSlashMatcher.matches()).as("with trailing slash matches").isTrue();
        assertThat(queryMatcher.matches()).as("with query string matches").isTrue();
    }

    @Test
    public void testParameterPath() {
        final String path = "/api/items/{groupid}/{itemid}/";

        final CompiledPath compiled = this.compiler.compile(path, this.requirements);

        final Matcher shortSlashMatcher = compiled.getPattern().matcher("/api/items/");

        final Matcher fullMatcher =
                compiled.getPattern().matcher("/api/items/group1/item3?foo=bar");

        assertThat(compiled.getParameters()).as("parameters").contains("groupid", "itemid");
        assertThat(shortSlashMatcher.matches()).as("short matcher").isFalse();
        assertThat(fullMatcher.matches()).as("full matcher").isTrue();

        final String groupId = fullMatcher.group("groupid");
        final String itemId  = fullMatcher.group("itemid");
        final String query   = fullMatcher.group("QUERY");

        assertThat(groupId).as("groupId group").isEqualTo("group1");
        assertThat(itemId).as("itemId group").isEqualTo("item3");
        assertThat(query).as("query string").isEqualTo("?foo=bar");
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

        assertThat(compiled.getParameters()).as("parameters").hasSize(2);
        assertThat(correctMatcher.matches()).as("correct matcher").isTrue();
        assertThat(badGroupMatcher.matches()).as("bad group matcher").isFalse();
        assertThat(badItemMatcher.matches()).as("bad item matcher").isFalse();
    }

    @Test
    public void testUnterminatedParameterBlock() {
        final String path = "/api/items/{groupid/";

        assertThrows(BadRoutePathException.class, () ->
                this.compiler.compile(path, this.requirements)
        );
    }

    @Test
    public void testBadParameterName() {
        final String path = "/api/items/{item_id}";

        assertThrows(BadRoutePathException.class, () ->
                this.compiler.compile(path, this.requirements)
        );
    }
}
