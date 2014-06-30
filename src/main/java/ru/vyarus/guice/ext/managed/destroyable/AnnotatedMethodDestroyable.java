package ru.vyarus.guice.ext.managed.destroyable;

import java.lang.reflect.Method;

/**
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public class AnnotatedMethodDestroyable implements Destroyable {

    private Method method;
    private Object instance;

    public AnnotatedMethodDestroyable(Method method, Object instance) {
        this.method = method;
        this.instance = instance;
    }

    @Override
    public void preDestroy() throws Exception{
        method.invoke(instance);
    }
}
