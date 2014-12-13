package ru.vyarus.guice.ext.generator.support;

import ru.vyarus.guice.ext.generator.support.aop.CustomAop;

/**
 * @author Vyacheslav Rusakov
 * @since 10.12.2014
 */
public abstract class AbstractBean {

    @CustomAop
    public abstract String hello();
}
