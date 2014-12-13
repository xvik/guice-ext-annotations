package ru.vyarus.guice.ext.generator.support.ctor;

import com.google.inject.Inject;
import com.google.inject.ProvidedBy;
import com.google.inject.internal.DynamicClassProvider;
import ru.vyarus.guice.ext.generator.support.ProvidedInterfaceBean;

/**
 * @author Vyacheslav Rusakov
 * @since 10.12.2014
 */
@ProvidedBy(DynamicClassProvider.class)
public abstract class CustomConstructorBean {

    ProvidedInterfaceBean bean;

    @Inject
    @Ann("ctor")
    public CustomConstructorBean(@Ann("param") ProvidedInterfaceBean bean) throws Exception {
        this.bean = bean;
    }

    public String hello() {
        return bean.hello();
    }
}
