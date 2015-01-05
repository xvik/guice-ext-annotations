package ru.vyarus.guice.ext.generator.support.ctor;

import com.google.inject.ProvidedBy;
import com.google.inject.internal.DynamicSingletonProvider;
import ru.vyarus.guice.ext.generator.support.ProvidedInterfaceBean;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author Vyacheslav Rusakov
 * @since 05.01.2015
 */
@ProvidedBy(DynamicSingletonProvider.class)
public abstract class GenerifiedConstructorBean {

    Provider<ProvidedInterfaceBean> beanProvider;

    @Inject
    public GenerifiedConstructorBean(Provider<ProvidedInterfaceBean> beanProvider) throws Exception {
        this.beanProvider = beanProvider;
    }

    public String hello() {
        return beanProvider.get().hello();
    }
}
