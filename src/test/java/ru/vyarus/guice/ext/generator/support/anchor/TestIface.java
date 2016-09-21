package ru.vyarus.guice.ext.generator.support.anchor;

import com.google.inject.ProvidedBy;
import com.google.inject.internal.DynamicSingletonProvider;
import ru.vyarus.guice.ext.generator.support.aop.CustomAop;

/**
 * @author Vyacheslav Rusakov
 * @since 21.09.2016
 */
@ProvidedBy(DynamicSingletonProvider.class)
public interface TestIface {

    @CustomAop
    String hello();
}
