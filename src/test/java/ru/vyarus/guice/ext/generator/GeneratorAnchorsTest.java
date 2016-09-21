package ru.vyarus.guice.ext.generator;

import com.google.inject.*;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Assert;
import org.junit.Test;
import ru.vyarus.guice.ext.core.generator.anchor.GeneratorAnchorModule;
import ru.vyarus.guice.ext.generator.support.anchor.CtorAbstractClass;
import ru.vyarus.guice.ext.generator.support.anchor.DynamicService;
import ru.vyarus.guice.ext.generator.support.anchor.PureAbstractClass;
import ru.vyarus.guice.ext.generator.support.anchor.TestIface;
import ru.vyarus.guice.ext.generator.support.aop.CustomAop;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author Vyacheslav Rusakov
 * @since 21.09.2016
 */
public class GeneratorAnchorsTest {

    @Test(expected = AbstractMethodError.class)
    public void checkChildInjectorFailure() throws Exception {
        // aop registered in child injector
        Injector injector = Guice.createInjector().createChildInjector(new ChildAopModule());
        // bean will be generated at root injector and so no aop will apply on it
        injector.getInstance(TestIface.class).hello();
    }

    @Test
    public void testAnchorModule() throws Exception {
        // aop registered in child injector with anchor module
        Injector injector = Guice.createInjector().createChildInjector(new ChildAopModule(), new GeneratorAnchorModule());
        injector.getInstance(TestIface.class).hello();
        injector.getInstance(PureAbstractClass.class).hello();
        // important: RootService not registered and so will be created by JIT in root injector
        injector.getInstance(CtorAbstractClass.class).hello();
    }

    @Test
    public void dynamicInjectionTest() throws Exception {
        Injector injector = Guice.createInjector()
                .createChildInjector(new ChildAopModule(), new GeneratorAnchorModule());
        // service resolved with jit still injects correct dependency
        injector.getInstance(DynamicService.class).hello();
    }

    @Test
    public void testPrivateModules() throws Exception {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(new MyPrivateModule("test"));
                install(new MyPrivateModule("other"));
            }
        });
        Assert.assertEquals(injector.getInstance(Key.get(TestIface.class, Names.named("test"))).hello(), "test");
        Assert.assertEquals(injector.getInstance(Key.get(TestIface.class, Names.named("other"))).hello(), "other");
    }

    static class ChildAopModule extends AbstractModule {

        String res;

        public ChildAopModule() {
            this("");
        }

        public ChildAopModule(String res) {
            this.res = res;
        }

        @Override
        protected void configure() {
            bindInterceptor(Matchers.any(), Matchers.annotatedWith(CustomAop.class), new MethodInterceptor() {
                @Override
                public Object invoke(MethodInvocation invocation) throws Throwable {
                    return res;
                }
            });
        }
    }

    private static class MyPrivateModule extends PrivateModule {

        private String res;

        public MyPrivateModule(String res) {
            this.res = res;
        }

        @Override
        protected void configure() {
            install(new ChildAopModule(res));
            install(new GeneratorAnchorModule());

            // duplicate binding required for exposing (otherwise only provider method could be used)
            bind(TestIface.class).annotatedWith(Names.named(res)).toProvider(ExposedApi.class);

            expose(TestIface.class).annotatedWith(Names.named(res));
        }

//        @Provides
//        @Exposed
//        @Named("test")
//        TestIface provide(TestIface serv) {
//            return serv;
//        }
    }

    private static class ExposedApi implements Provider<TestIface> {
        @Inject
        TestIface iface;

        @Override
        public TestIface get() {
            return iface;
        }
    }

}
