package ru.vyarus.guice.ext.generator.support.aop;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Vyacheslav Rusakov
 * @since 10.12.2014
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface CustomAop {
}
