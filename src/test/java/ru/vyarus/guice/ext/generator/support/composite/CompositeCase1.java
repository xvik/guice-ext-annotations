package ru.vyarus.guice.ext.generator.support.composite;

import com.google.inject.ProvidedBy;
import com.google.inject.internal.DynamicClassProvider;
import ru.vyarus.guice.ext.generator.support.aop.CustomAop;

/**
 * Additional method with direct aop
 *
 * @author Vyacheslav Rusakov
 * @since 10.12.2014
 */
@ProvidedBy(DynamicClassProvider.class)
public abstract class CompositeCase1 {

    public String self(){
        return "self";
    }

    @CustomAop
    public abstract String hello();
}
