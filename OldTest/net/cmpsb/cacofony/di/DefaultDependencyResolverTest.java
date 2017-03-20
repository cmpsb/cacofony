package net.cmpsb.cacofony.di;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the default dependency resolver.
 *
 * @author Luc Everse
 */
public class DefaultDependencyResolverTest {
    private DependencyResolver resolver;

    @Before
    public void before() {
        this.resolver = new DefaultDependencyResolver();
    }

    @Test(expected = UnresolvableDependencyException.class)
    public void testPrivateClass() {
        this.resolver.get(PrivateClass.class);
    }

    @Test
    public void testPublicClass() {
        PublicClass instance = this.resolver.get(PublicClass.class);

        assertTrue(instance.getTrue());
    }

    @Test(expected = UnresolvableDependencyException.class)
    public void testSemiPublicClass() {
        this.resolver.get(SemiPublicClass.class);
    }

    @Test(expected = UnresolvableDependencyException.class)
    public void testUnsatisfiableClass() {
        this.resolver.get(UnsatisfiableClass.class);
    }

    @Test
    public void testDependentClass() {
        DependentClass instance = this.resolver.get(DependentClass.class);

        assertTrue(instance.getPublicClass().getTrue());
    }

    @Test
    public void testGetByName() {
        PublicClass instance = new PublicClass();

        this.resolver.add(instance);
        this.resolver.name(PublicClass.class, "Public.Class");

        PublicClass namedInstance = this.resolver.get("public.class", PublicClass.class);

        assertThat("The returned instance is the instance we just added.",
                   namedInstance,
                   is(instance));
    }

    @Test
    public void testGetByNameWithAutoInference() {
        PublicClass instance = this.resolver.get("Public.Class", PublicClass.class);

        assertTrue(instance.getTrue());
    }

    @Test
    public void testAutoNaming() {
        this.resolver.get(PublicClass.class);

        PublicClass instance = this.resolver.get(
                "net.cmpsb.cacofony.di.DefaultDependencyResolverTest.PublicClass",
                PublicClass.class);

        assertTrue(instance.getTrue());
    }

    @Test
    public void testInstantiateString() {
        this.resolver.get(String.class);
    }

    private final class PrivateClass {
        private PrivateClass() {
            // Does nothing.
        }
    }

    public final class PublicClass {
        public boolean getTrue() {
            return true;
        }
    }

    public final class SemiPublicClass {
        private SemiPublicClass() {
            // Does nothing.
        }
    }

    public final class DependentClass {
        private final PublicClass publicClass;

        public DependentClass(final PublicClass otherClass) {
            this.publicClass = otherClass;
        }

        public PublicClass getPublicClass() {
            return this.publicClass;
        }
    }

    public final class UnsatisfiableClass {
        public UnsatisfiableClass(final int value) {
            // Does nothing.
        }
    }
}
