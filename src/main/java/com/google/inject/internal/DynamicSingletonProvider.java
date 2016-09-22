package com.google.inject.internal;

import com.google.inject.Injector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;

/**
 * Specific version of {@link com.google.inject.internal.DynamicClassProvider}, which applies singleton scope
 * to generated classes.
 * The main intention is to reduce code size from common case from
 * {@code  @ScopeAnnotation(Singleton.class) @ProvidedBy(DynamicClassProvider.class)}
 * to simply {@code @ProvidedBy(DynamicSingletonProvider.class)}.
 * <p>
 * If used with injectors hierarchy or within private modules, use together with
 * {@link ru.vyarus.guice.ext.core.generator.anchor.GeneratorAnchorModule} to properly scope dynamic bindings.
 *
 * @author Vyacheslav Rusakov
 * @see com.google.inject.internal.DynamicClassProvider for more docs
 * @since 05.01.2015
 */
@Singleton
public class DynamicSingletonProvider extends DynamicClassProvider {

    @Inject
    public DynamicSingletonProvider(final Injector injector) {
        super(injector);
    }

    @Override
    protected Class<? extends Annotation> getScopeAnnotation() {
        return Singleton.class;
    }
}
