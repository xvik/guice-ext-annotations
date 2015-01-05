package ru.vyarus.guice.ext.generator;

import com.google.inject.*;
import com.google.inject.matcher.Matchers;
import org.junit.Assert;
import org.junit.Test;
import ru.vyarus.guice.ext.core.generator.DynamicClassException;
import ru.vyarus.guice.ext.core.generator.DynamicClassGenerator;
import ru.vyarus.guice.ext.generator.support.*;
import ru.vyarus.guice.ext.generator.support.aop.CustomAop;
import ru.vyarus.guice.ext.generator.support.aop.CustomAopInterceptor;
import ru.vyarus.guice.ext.generator.support.bad.BadDeclarationBean;
import ru.vyarus.guice.ext.generator.support.bad.BadSingletonDeclaration;
import ru.vyarus.guice.ext.generator.support.bad.WrongUsageBean;
import ru.vyarus.guice.ext.generator.support.composite.CompositeCase1;
import ru.vyarus.guice.ext.generator.support.composite.CompositeCase2;
import ru.vyarus.guice.ext.generator.support.ctor.Ann;
import ru.vyarus.guice.ext.generator.support.ctor.CustomConstructorBean;
import ru.vyarus.guice.ext.generator.support.ctor.GenerifiedConstructorBean;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Vyacheslav Rusakov
 * @since 10.12.2014
 */
public class GeneratorTest {

    @Test
    public void testClassGenerationCached() throws Exception {
        Class cl1 = DynamicClassGenerator.generate(InterfaceBean.class);
        // this time class should not be generated
        Class cl2 = DynamicClassGenerator.generate(InterfaceBean.class);
        assertTrue(cl1 == cl2);
    }

    @Test
    public void testDirectRegistration() throws Exception {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(InterfaceBean.class).to(DynamicClassGenerator.generate(InterfaceBean.class));
                bind(AbstractBean.class).to(DynamicClassGenerator.generate(AbstractBean.class));
            }
        });
        injector.getInstance(InterfaceBean.class);
        injector.getInstance(AbstractBean.class);
    }

    @Test
    public void testIndirectRegistration() throws Exception {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @SuppressWarnings("PointlessBinding")
            @Override
            protected void configure() {
                bind(ProvidedInterfaceBean.class);
                bind(ProvidedAbstractBean.class);
            }
        });
        injector.getInstance(ProvidedInterfaceBean.class);
        injector.getInstance(ProvidedAbstractBean.class);
    }

    @Test
    public void testJITResolution() throws Exception {
        Injector injector = Guice.createInjector();
        injector.getInstance(ProvidedInterfaceBean.class);
        injector.getInstance(ProvidedAbstractBean.class);
    }

    @Test
    public void testAnnotationPropagation() throws Exception {
        Injector injector = Guice.createInjector();
        ProvidedInterfaceBean interfaceInstance = injector.getInstance(ProvidedInterfaceBean.class);
        ProvidedAbstractBean beanInstance = injector.getInstance(ProvidedAbstractBean.class);
        // singleton scope declared on interface and propagated
        assertEquals(interfaceInstance, injector.getInstance(ProvidedInterfaceBean.class));
        // no scope definition - prototype
        Assert.assertNotEquals(beanInstance, injector.getInstance(ProvidedAbstractBean.class));
    }

    @Test
    public void testAnnotationCopyCorrectness() throws Exception {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(InterfaceBean.class).to(DynamicClassGenerator.generate(InterfaceBean.class));
            }
        });
        InterfaceBean bean = injector.getInstance(InterfaceBean.class);
        AnnotationCheck ann = bean.getClass().getAnnotation(AnnotationCheck.class);
        assertTrue(ann.ann().value() == Singleton.class);
        assertTrue(Arrays.equals(ann.arr(), new int[]{1, 2, 3}));
        assertTrue(Arrays.equals(ann.arrBool(), new boolean[]{true, false}));
        assertTrue(Arrays.equals(ann.arrByte(), new byte[]{1, 2}));
        assertTrue(Arrays.equals(ann.arrChar(), new char[]{',', 'l'}));
        assertTrue(Arrays.equals(ann.arrDbl(), new double[]{0.1, 0.2}));
        assertTrue(Arrays.equals(ann.arrFlt(), new float[]{0.1f, 0.2f}));
        assertTrue(Arrays.equals(ann.arrLng(), new long[]{1l, 2l}));
        assertTrue(Arrays.equals(ann.arrObj(), new String[]{"tst", "tst2"}));
        assertTrue(ann.bool());
        assertTrue(ann.bte() == 1);
        assertTrue(ann.chr() == ',');
        assertTrue(ann.dbl() == 1d);
        assertTrue(ann.integer() == 1);
        assertTrue(ann.lng() == 1l);
        assertTrue(ann.flt() == 1f);
        assertTrue(ann.srt() == 1);
        assertTrue(ann.str().equals("string"));
        assertTrue(ann.cls() == InterfaceBean.class);
        assertTrue(ann.enm() == ElementType.TYPE);
    }

    @Test(expected = DynamicClassException.class)
    public void testDirectScopeAnnotation() throws Exception {
        // scope annotation can't be used directly
        DynamicClassGenerator.generate(BadDeclarationBean.class);
    }

    @Test
    public void testAop() throws Exception {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(InterfaceBean.class).to(DynamicClassGenerator.generate(InterfaceBean.class));
                bind(AbstractBean.class).to(DynamicClassGenerator.generate(AbstractBean.class));
                bindInterceptor(Matchers.any(), Matchers.annotatedWith(CustomAop.class), new CustomAopInterceptor());
            }
        });
        injector.getInstance(InterfaceBean.class).hello();
        injector.getInstance(AbstractBean.class).hello();
        injector.getInstance(ProvidedInterfaceBean.class).hello();
        injector.getInstance(ProvidedAbstractBean.class).hello();
    }

    @Test(expected = AbstractMethodError.class)
    public void testNoAopCall() throws Exception {
        Guice.createInjector().getInstance(ProvidedInterfaceBean.class).badMethod();
    }

    @Test
    public void testCompositeCases() throws Exception {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bindInterceptor(Matchers.any(), Matchers.annotatedWith(CustomAop.class), new CustomAopInterceptor());
            }
        });
        CompositeCase1 case1 = injector.getInstance(CompositeCase1.class);
        CompositeCase2 case2 = injector.getInstance(CompositeCase2.class);

        case1.self();
        case1.hello();

        case2.self();
        case2.hello();
    }

    @Test(expected = ProvisionException.class)
    public void testWrongProviderUsage() throws Exception {
        Guice.createInjector().getInstance(WrongUsageBean.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCustomConstructor() throws Exception {
        // original type constructor copied and constructor injection correctly handled
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bindInterceptor(Matchers.any(), Matchers.annotatedWith(CustomAop.class), new CustomAopInterceptor());
            }
        }).getInstance(CustomConstructorBean.class).hello();
        Class<?> generated = DynamicClassGenerator.generate(CustomConstructorBean.class);
        assertEquals(1, generated.getConstructors().length);
        Constructor ctor = generated.getConstructors()[0];
        assertTrue(ctor.isAnnotationPresent(Inject.class));
        assertTrue(ctor.isAnnotationPresent(Ann.class));
        assertEquals("ctor", ((Ann) ctor.getAnnotation(Ann.class)).value());
        assertEquals(1, ctor.getExceptionTypes().length);
        assertEquals(1, ctor.getParameterAnnotations()[0].length);
        Annotation ann = ctor.getParameterAnnotations()[0][0];
        assertTrue(ann instanceof Ann);
        assertEquals("param", ((Ann) ann).value());
    }

    @Test
    public void testGenerifiedConstructor() throws Exception {
        // original type constructor copied and constructor injection correctly handled
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bindInterceptor(Matchers.any(), Matchers.annotatedWith(CustomAop.class), new CustomAopInterceptor());
            }
        }).getInstance(GenerifiedConstructorBean.class).hello();
    }

    @Test
    public void testSingletonProvider() throws Exception {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bindInterceptor(Matchers.any(), Matchers.annotatedWith(CustomAop.class), new CustomAopInterceptor());
            }
        });
        GenerifiedConstructorBean bean1 = injector.getInstance(GenerifiedConstructorBean.class);
        GenerifiedConstructorBean bean2 = injector.getInstance(GenerifiedConstructorBean.class);
        Assert.assertTrue(bean1 == bean2);
    }

    @Test(expected = ProvisionException.class)
    public void testDuplicateSingletonDeclaration() throws Exception {
        Guice.createInjector().getInstance(BadSingletonDeclaration.class);

    }
}
