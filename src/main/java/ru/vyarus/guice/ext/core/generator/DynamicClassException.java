package ru.vyarus.guice.ext.core.generator;

/**
 * Indicates error during dynamic class generation.
 *
 * @author Vyacheslav Rusakov
 * @see ru.vyarus.guice.ext.core.generator.DynamicClassGenerator
 * @since 10.12.2014
 */
public class DynamicClassException extends RuntimeException {

    public DynamicClassException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
