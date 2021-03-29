package com.google.inject.internal;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import ru.vyarus.guice.ext.core.generator.DynamicClassGenerator;
import ru.vyarus.guice.ext.core.generator.anchor.AnchorBean;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;

/**
 * Provider allows using interfaces or abstract classes as normal guice beans.
 * Simply annotate interface or abstract class with {@code @ProvidedBy(DynamicClassProvider.class)}
 * and use it as usual guice bean.
 * <p>Original bean will be correctly handled by guice aop: provider will generate new class, which guice could
 * use for proxy generation.</p>
 * <p>If used with injectors hierarchy or within private modules, use together with
 * {@link ru.vyarus.guice.ext.core.generator.anchor.GeneratorAnchorModule} to properly scope dynamic bindings.</p>
 * <p>Providers use guice package to use internal guice api (to resolve actual required type)</p>
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.guice.ext.core.generator.DynamicClassGenerator if you prefer direct registration in module
 * @since 07.12.2014
 */
@Singleton
public class DynamicClassProvider implements Provider<Object> {

    private final Injector injector;

    @Inject
    public DynamicClassProvider(final Injector injector) {
        this.injector = injector;
    }

    @Override
    @SuppressWarnings({"PMD.PreserveStackTrace", "PMD.NullAssignment"})
    public Object get() {
        try (InternalContext context = ((InjectorImpl) injector).enterContext()) {
            // check if (possibly) child context contains anchor bean definition
            final boolean hasAnchor = injector.getExistingBinding(Key.get(AnchorBean.class)) != null;
            final Class<?> abstractType = context.getDependency().getKey().getTypeLiteral().getRawType();
            final Class<?> generatedType = DynamicClassGenerator.generate(abstractType, getScopeAnnotation(),
                    hasAnchor ? AnchorBean.class : null);
            return injector.getInstance(generatedType);
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
