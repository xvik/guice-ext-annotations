package ru.vyarus.guice.ext.generator.support.composite;

import com.google.inject.ProvidedBy;
import com.google.inject.internal.DynamicClassProvider;
import ru.vyarus.guice.ext.generator.support.InterfaceBean;

/**
 * Additional methods provided by interface
 *
 * @author Vyacheslav Rusakov
 * @since 10.12.2014
 */
@ProvidedBy(DynamicClassProvider.class)
public abstract class CompositeCase2 implements InterfaceBean {

    public String self(){
        return "self";
    }
}
