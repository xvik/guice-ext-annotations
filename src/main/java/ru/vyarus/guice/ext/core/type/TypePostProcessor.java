package ru.vyarus.guice.ext.core.type;

/**
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public interface TypePostProcessor<T> {
    void process(T instance);
}
