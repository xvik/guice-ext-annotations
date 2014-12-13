package ru.vyarus.guice.ext.generator.support.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author Vyacheslav Rusakov
 * @since 10.12.2014
 */
public class CustomAopInterceptor implements MethodInterceptor{

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        return "I'm intercepted!";
    }
}
