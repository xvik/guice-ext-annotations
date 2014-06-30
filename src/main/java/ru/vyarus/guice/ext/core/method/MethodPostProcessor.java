package ru.vyarus.guice.ext.core.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public interface MethodPostProcessor<T extends Annotation> {

   void process(T annotation, Method method, Object instance) throws Exception;
}
