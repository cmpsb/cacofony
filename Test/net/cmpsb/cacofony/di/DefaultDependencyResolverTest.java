package net.cmpsb.cacofony.di;

import net.cmpsb.cacofony.di.classes.BadStrategy;
import net.cmpsb.cacofony.di.classes.ComplexNested;
import net.cmpsb.cacofony.di.classes.MultiInstanceClass;
import net.cmpsb.cacofony.di.classes.Complex;
import net.cmpsb.cacofony.di.classes.NullaryConstructor;
import net.cmpsb.cacofony.di.classes.PrivateConstructor;
import net.cmpsb.cacofony.di.classes.PrivateDependentConstructor;
import net.cmpsb.cacofony.di.classes.RiskyConstructor;
import net.cmpsb.cacofony.di.classes.ShitloadOfAnnotations;
import net.cmpsb.cacofony.di.classes.SimpleImplicit;
import net.cmpsb.cacofony.util.Ob;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

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

    @Test
    public void testNullaryConstructor() {
        final NullaryConstructor instance = this.resolver.get(NullaryConstructor.class);

        assertNotNull(instance);
    }

    @Test
    public void testSimpleImplicitInjection() {
        final SimpleImplicit instance = this.resolver.get(SimpleImplicit.class);

        assertNotNull(instance);

        assertEquals(instance.getLeft(), instance.getRight());
    }

    @Test
    public void testMultiInstance() {
        final MultiInstanceClass etaoin = this.resolver.get(MultiInstanceClass.class);
        final MultiInstanceClass shrdlu = this.resolver.get(MultiInstanceClass.class);

        assertNotNull(etaoin);
        assertNotEquals(etaoin, shrdlu);
    }

    @Test
    public void testComplex() {
        final int count = 3;
        final String fieldDep = "Injected through field injection!";

        final Complex instance = this.resolver.get(
                Ob.map(
                        "count", count,
                        "field-dep", fieldDep
                ),
                Complex.class
        );

        assertNotNull(instance);
        assertNotNull(instance.getOne());
        assertNotNull(instance.getOther());
        assertNotNull(instance.getFieldDep());
        assertEquals(instance.getFieldDep(), fieldDep);
        assertEquals(instance.getCount(), count);
    }

    @Test
    public void testComplexNested() {
        final int count = 3;
        final String fieldDep = "Injected through field injection!";

        final Complex instance = this.resolver.get(
                Ob.map(
                        "count", count,
                        "field-dep", fieldDep
                ),
                ComplexNested.class
        );

        assertNotNull(instance);
        assertNotNull(instance.getOne());
        assertNotNull(instance.getOther());
        assertNotNull(instance.getFieldDep());
        assertEquals(instance.getFieldDep(), fieldDep);
        assertEquals(instance.getCount(), count);
    }

    @Test
    public void testShitloadOfAnnotations() {
        final String named = this.resolver.get("named", String.class);
        final ShitloadOfAnnotations instance = this.resolver.get(
            Ob.map(
                "one", "one",
                "two", "two",
                "three", "three",
                "four", "four"
            ),
            ShitloadOfAnnotations.class
        );

        assertNotNull(instance);
        assertEquals(instance.getValue(), named.hashCode() + "onetwothreefour");
    }

    @Test(expected = UnknownInjectionStrategyException.class)
    public void testBadStrategy() {
        this.resolver.get(BadStrategy.class);
    }

    @Test(expected = UnresolvableDependencyException.class)
    public void testPrivateConstructor() {
        this.resolver.get(PrivateConstructor.class);
    }

    @Test(expected = Exception.class)
    public void testRiskyConstructor() {
        this.resolver.get(RiskyConstructor.class);
    }

    @Test(expected = UnresolvableDependencyException.class)
    public void testPrivateDependentConstructor() {
        this.resolver.get(PrivateDependentConstructor.class);
    }

    @Test(expected = UnresolvableDependencyException.class)
    public void testPrivateInnerClass() {
        this.resolver.get(PrivateInnerClass.class);
    }

    private class PrivateInnerClass {

    }
}
