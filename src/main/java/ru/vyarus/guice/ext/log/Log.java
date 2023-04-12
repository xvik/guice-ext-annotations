package ru.vyarus.guice.ext.log;

import com.google.inject.ScopeAnnotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Log annotation must be used on {@code org.slf4j.Logger} fields to automatically inject logger instance.
 */
@ScopeAnnotation
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface Log {

}
