package ru.vyarus.guice.ext.core.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Annotated method post processor.
 *
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public interface MethodPostProcessor<T extends Annotation> {

    /**
     * Called to post process annotated bean method.
     * It is safe to avoid explicit exception handling (except special cases required by processor logic).
     *
     * @param annotation annotation instance
     * @param method     annotated method
     * @param instance   bean instance
     * @throws Exception on any unrecoverable error
     * @see ru.vyarus.guice.ext.core.method.AnnotatedMethodTypeListener
     */
    void process(T annotation, Method method, Object instance) throws Exception;
}
