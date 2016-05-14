package com.google.inject.internal;

import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import ru.vyarus.guice.ext.core.generator.GeneratorClassLoader;
import ru.vyarus.guice.ext.core.generator.DynamicClassGenerator;

import javax.inject.Inject;
import java.lang.annotation.Annotation;

/**
 * Provider allows using interfaces or abstract classes as normal guice beans.
 * Simply annotate interface or abstract class with {@code @ProvidedBy(DynamicClassProvider.class)}
 * and use it as usual guice bean.
 * <p>Original bean will be correctly handled by guice aop: provider will generate new class, which guice could
 * use for proxy generation.</p>
 * <p>Providers use guice package to use internal guice api (to resolve actual required type)</p>
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.guice.ext.core.generator.DynamicClassGenerator if you prefer direct registration in module
 * @since 07.12.2014
 */
public class DynamicClassProvider implements Provider<Object> {

    private final Injector injector;

    @Inject
    public DynamicClassProvider(final Injector injector) {
        this.injector = injector;
    }

    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
    public Object get() {
        try {
            return ((InjectorImpl) injector).callInContext(new ContextualCallable<Object>() {
                @Override
                public Object call(final InternalContext context) {
                    final Class<?> abstractType = context.getDependency().getKey().getTypeLiteral().getRawType();
                    final Class<?> generatedType = DynamicClassGenerator.generate(abstractType,
                            getScopeAnnotation(),
                            injector.getInstance(GeneratorClassLoader.class).getLoader());
                    return injector.getInstance(generatedType);
                }
            });
        } catch (ErrorsException e) {
            throw new ProvisionException(e.getErrors().getMessages());
        }
    }

    /**
     * Override it to specify different annotation. By default, no annotation specified which will implicitly lead
     * to default prototype scope.
     *
     * @return scope annotation which should be applied to generated class
     */
    protected Class<? extends Annotation> getScopeAnnotation() {
        return null;
    }
}
