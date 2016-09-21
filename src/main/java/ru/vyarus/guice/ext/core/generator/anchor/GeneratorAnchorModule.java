package ru.vyarus.guice.ext.core.generator.anchor;

import com.google.inject.AbstractModule;
import com.google.inject.internal.DynamicClassProvider;
import com.google.inject.internal.DynamicSingletonProvider;

import javax.inject.Singleton;

/**
 * Support module used to tie dynamic binding for generated class (generated with {@link DynamicClassProvider}) to
 * exact injector in injectors hierarchy. Also, allows using JIT resolution for generated classes in private modules
 * (without binding them explicitly).
 * <p>
 * Situation without this module: suppose we have root and child injector and aop interceptors
 * (which must intercept generated abstract method calls) are registered in child module. If abstract bean
 * resolved with guice JIT ({@code @ProvidedBy(DynamicClassProvider.class)} then it's binding will be created
 * in upper-most injector: root injector. As a result, calling any abstract method will fail, because interceptors
 * are not present in root module (only in child module).
 * <p>
 * To fix this problem we need to "tie" generated class binding to child injector. Guice will not bubble it up
 * only if it depends on some other bean located in child injector. For this purpose, module registers
 * dummy service {@link AnchorBean} which is only required to "hold" generated been in child injector.
 * Also, provider classes are explicitly bound to be able to operate with child injector in provider.
 * Both {@link DynamicClassProvider} and {@link DynamicSingletonProvider} will check first if anchor bean
 * is available in current injector and inject dependency on this bean into generated class (add new constructor
 * parameter for abstract class with constructor and generate new constructor for interface implementation or
 * abstract class without constructor).
 * <p>
 * Also, module will prevent improper guice injector usage. For example, anchor module registered in child injector
 * and some service DynService depends (injects) on abstract class annotated
 * with {@code ProvidedBy(DynamicClassProvider.class)}. If DynService is not bound in child injector and we try
 * to create instance of (using JIT binding) it from root injector, then guice will try to obtain DynamicClassProvider
 * in root context and fail with duplicate binding definition (without anchor module, everything would pass,
 * but aop will not work, because it was registered in child injector).
 * <p>
 * Example usage:
 * <pre><code>
 *     Injector childInjector = Guice.createInjector() // root injector
 *                              .createChildInjector(new GeneratorAnchorModule(), new MyAopModule());
 *     // JIT binding for MyAbstractService (generated class) will be in child injector and so aop will work
 *     childInjector.getInstance(MyAbstractService.class).someAMethodHandledWithAop();
 * </code></pre>
 * <p>
 * Private module example:
 * <pre><code>
 *     public class MyPrivateModule extends PrivateModule {
 *         protected void configure() {
 *             install(new MyAopModule());
 *             install(new GeneratorAnchorModule());
 *         }
 *
 *         {@literal @}Provides @Exposed @Named("myBean")
 *         MyAbstractBean provide(MyAbstractBean bean) {
 *             return bean;
 *         }
 *     }
 *
 *     Injector injector = Injector.createModule(new MyPrivateModule());
 *     // obtain exposed named instance outside of private module
 *     // note that abstract bean was not bound and resolved with JIT
 *     injector.getInstance(Key.get(MyAbstractBean.class, Names.named("myBean")))
 * </code></pre>
 *
 * @author Vyacheslav Rusakov
 * @since 21.09.2016
 */
public class GeneratorAnchorModule extends AbstractModule {

    @Override
    protected void configure() {
        // providers bound explicitly to tie them to child injector
        bind(DynamicClassProvider.class);
        bind(DynamicSingletonProvider.class);
        // anchor bean used to tie dynamically provided beans to child injector
        bind(AnchorBean.class).in(Singleton.class);
    }
}
