package net.cmpsb.cacofony.di;

import net.cmpsb.cacofony.di.classes.ComplexNested;
import net.cmpsb.cacofony.di.classes.Complex;
import net.cmpsb.cacofony.di.classes.NullaryConstructor;
import net.cmpsb.cacofony.di.classes.PrivateConstructor;
import net.cmpsb.cacofony.di.classes.PrivateDependentConstructor;
import net.cmpsb.cacofony.di.classes.RiskyConstructor;
import net.cmpsb.cacofony.di.classes.SimpleImplicit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the default dependency resolver.
 *
 * @author Luc Everse
 */
public class DefaultDependencyResolverTest {
    private DependencyResolver resolver;

    @BeforeEach
    public void before() {
        this.resolver = new DependencyResolver();
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
    public void testComplex() {
        final int count = 3;
        final String fieldDep = "Injected through field injection!";
        this.resolver.add(String.class, fieldDep);
        this.resolver.add(Integer.class, count);

        final Complex instance = this.resolver.get(Complex.class);

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
        this.resolver.add(String.class, fieldDep);
        this.resolver.add(Integer.class, count);

        final Complex instance = this.resolver.get(ComplexNested.class);

        assertNotNull(instance);
        assertNotNull(instance.getOne());
        assertNotNull(instance.getOther());
        assertNotNull(instance.getFieldDep());
        assertEquals(instance.getFieldDep(), fieldDep);
        assertEquals(instance.getCount(), count);
    }

    @Test
    public void testPrivateConstructor() {
        assertThrows(UnresolvableDependencyException.class, () ->
                this.resolver.get(PrivateConstructor.class)
        );
    }

    @Test
    public void testRiskyConstructor() {
        assertThrows(Exception.class, () -> this.resolver.get(RiskyConstructor.class));
    }

    @Test
    public void testPrivateDependentConstructor() {
        assertThrows(UnresolvableDependencyException.class, () ->
                this.resolver.get(PrivateDependentConstructor.class)
        );
    }

    @Test
    public void testPrivateInnerClass() {
        assertThrows(UnresolvableDependencyException.class, () ->
                this.resolver.get(PrivateInnerClass.class)
        );
    }

    private class PrivateInnerClass {

    }
}
