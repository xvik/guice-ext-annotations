package ru.vyarus.guice.ext.core.generator;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Guice doesn't allow scope annotations on abstract types
 * ({@code com.google.inject.internal.Annotations#checkForMisplacedScopeAnnotations()}), so you can't use it directly.
 * <p>This annotation wraps actual scope annotation which will be set to generated class.</p>
 * <p>And yes, it's named as guice marker annotation, but name is perfect and you'll never need to use both in one
 * class.</p>
 *
 * @author Vyacheslav Rusakov
 * @since 10.12.2014
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface ScopeAnnotation {

    /**
     * @return scope annotation
     */
    Class<? extends Annotation> value();
}
