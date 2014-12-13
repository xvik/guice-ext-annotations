package ru.vyarus.guice.ext.generator.support.ctor;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Vyacheslav Rusakov
 * @since 11.12.2014
 */
@Target({PARAMETER, CONSTRUCTOR})
@Retention(RUNTIME)
public @interface Ann {

    String value();
}
