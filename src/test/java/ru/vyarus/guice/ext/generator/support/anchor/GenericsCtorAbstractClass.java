package ru.vyarus.guice.ext.generator.support.anchor;

import com.google.inject.ProvidedBy;
import com.google.inject.internal.DynamicSingletonProvider;
import ru.vyarus.guice.ext.generator.support.aop.CustomAop;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Vyacheslav Rusakov
 * @since 22.09.2016
 */
@ProvidedBy(DynamicSingletonProvider.class)
public abstract class GenericsCtorAbstractClass {

    private final Provider<RootService> service;

    @Inject
    public GenericsCtorAbstractClass(Provider<RootService> service) {
        this.service = service;
    }

    @CustomAop
    public abstract String hello();
}
