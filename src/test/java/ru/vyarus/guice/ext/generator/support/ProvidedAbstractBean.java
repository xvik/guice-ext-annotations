package ru.vyarus.guice.ext.generator.support;

import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import com.google.inject.internal.DynamicClassProvider;
import ru.vyarus.guice.ext.generator.support.aop.CustomAop;

/**
 * @author Vyacheslav Rusakov
 * @since 10.12.2014
 */
@ProvidedBy(DynamicClassProvider.class)
public abstract class ProvidedAbstractBean {

    @CustomAop
    public abstract String hello();
}
