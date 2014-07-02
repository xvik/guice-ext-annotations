package ru.vyarus.guice.ext.core.field;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import ru.vyarus.guice.ext.core.util.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Generic type listener to process annotated fields after bean instantiation.
 *
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public class AnnotatedFieldTypeListener<T extends Annotation> implements TypeListener {

    private Class<T> annotationClass;
    private FieldPostProcessor<T> postProcessor;

    public AnnotatedFieldTypeListener(final Class<T> annotationClass, final FieldPostProcessor<T> postProcessor) {
        this.annotationClass = annotationClass;
        this.postProcessor = postProcessor;
    }

    @Override
    public <I> void hear(final TypeLiteral<I> type, final TypeEncounter<I> encounter) {
        final Class<? super I> actualType = type.getRawType();
        if (!Utils.isPackageValid(actualType)) {
            return;
        }
        Class<? super I> investigatingType = actualType;
        while (investigatingType != null && !investigatingType.equals(Object.class)) {
            for (final Field field : investigatingType.getDeclaredFields()) {
                if (field.isAnnotationPresent(annotationClass)) {
                    encounter.register(new InjectionListener<I>() {
                        @Override
                        public void afterInjection(I injectee) {
                            try {
                                field.setAccessible(true);
                                postProcessor.process(field.getAnnotation(annotationClass), field, injectee);
                            } catch (Exception ex) {
                                throw new IllegalStateException(
                                        String.format("Failed to process annotation %s on field %s of class %s",
                                                annotationClass.getSimpleName(), field.getName(), injectee.getClass().getSimpleName()), ex);
                            }
                        }
                    });
                }
            }
            investigatingType = investigatingType.getSuperclass();
        }
    }
}
