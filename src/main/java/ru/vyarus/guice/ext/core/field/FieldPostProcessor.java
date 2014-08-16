package ru.vyarus.guice.ext.core.field;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Annotated filed post processor.
 *
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 * @param <T> annotation type
 */
public interface FieldPostProcessor<T extends Annotation> {

    /**
     * Called to post process annotated bean filed.
     * It is safe to avoid explicit exception handling (except special cases required by processor logic).
     *
     * @param annotation annotation instance
     * @param field      annotated field
     * @param instance   bean instance
     * @throws Exception on any unrecoverable error
     * @see ru.vyarus.guice.ext.core.field.AnnotatedFieldTypeListener
     */
    void process(T annotation, Field field, Object instance) throws Exception;
}
