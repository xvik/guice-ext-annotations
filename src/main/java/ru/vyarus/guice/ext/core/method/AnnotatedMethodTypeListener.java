package ru.vyarus.guice.ext.core.method;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import ru.vyarus.guice.ext.core.util.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Generic type listener to process annotated methods after bean instantiation.
 *
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 * @param <T> annotation type
 */
public class AnnotatedMethodTypeListener<T extends Annotation> implements TypeListener {

    private Class<T> annotationClass;
    private MethodPostProcessor<T> postProcessor;


    public AnnotatedMethodTypeListener(final Class<T> annotationClass,
                                       final MethodPostProcessor<T> postProcessor) {
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
            for (final Method method : investigatingType.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotationClass)) {
                    encounter.register(new InjectionListener<I>() {
                        @Override
                        public void afterInjection(final I injectee) {
                            try {
                                method.setAccessible(true);
                                postProcessor.process(method.getAnnotation(annotationClass), method, injectee);
                            } catch (Exception ex) {
                                throw new IllegalStateException(
                                        String.format("Failed to process annotation %s on method %s of class %s",
                                                annotationClass.getSimpleName(), method.getName(),
                                                injectee.getClass().getSimpleName()), ex);
                            }
                        }
                    });
                }
            }
            investigatingType = investigatingType.getSuperclass();
        }
    }
}
