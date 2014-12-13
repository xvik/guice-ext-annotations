package ru.vyarus.guice.ext.generator.support;

import com.google.inject.ProvidedBy;
import com.google.inject.Singleton;
import com.google.inject.internal.DynamicClassProvider;
import ru.vyarus.guice.ext.core.generator.ScopeAnnotation;
import ru.vyarus.guice.ext.generator.support.aop.CustomAop;

/**
 * @author Vyacheslav Rusakov
 * @since 10.12.2014
 */
@ScopeAnnotation(Singleton.class)
@ProvidedBy(DynamicClassProvider.class)
public interface ProvidedInterfaceBean {

    @CustomAop
    String hello();

    String badMethod();
}
