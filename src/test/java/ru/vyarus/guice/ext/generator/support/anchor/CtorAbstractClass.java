package ru.vyarus.guice.ext.generator.support.anchor;

import com.google.inject.ProvidedBy;
import com.google.inject.internal.DynamicSingletonProvider;
import ru.vyarus.guice.ext.generator.support.aop.CustomAop;

import javax.inject.Inject;

/**
 * @author Vyacheslav Rusakov
 * @since 21.09.2016
 */
@ProvidedBy(DynamicSingletonProvider.class)
public abstract class CtorAbstractClass {

    private final RootService service;

    @Inject
    public CtorAbstractClass(RootService service) {
        this.service = service;
    }

    @CustomAop
    public abstract String hello();
}
