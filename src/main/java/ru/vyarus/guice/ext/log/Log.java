package ru.vyarus.guice.ext.log;

import javax.inject.Scope;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Log annotation must be used on {@code org.slf4j.Logger} fields to automatically inject logger instance.
 */
@Scope
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface Log {

}
