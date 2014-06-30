package ru.vyarus.guice.ext.core.field;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public interface FieldPostProcessor<T extends Annotation> {

    void process(T annotation, Field field, Object instance) throws Exception;
}
