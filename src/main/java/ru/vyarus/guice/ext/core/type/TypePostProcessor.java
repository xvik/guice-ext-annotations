package ru.vyarus.guice.ext.core.type;

/**
 * Type post processor (type searched by exact type, subclass or implemented interface).
 *
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 * @param <T> bean type
 */
public interface TypePostProcessor<T> {

    /**
     * Called to post process bean.
     * It is safe to avoid explicit exception handling (except special cases required by processor logic).
     *
     * @param instance bean instance
     * @throws Exception on any unrecoverable error
     * @see ru.vyarus.guice.ext.core.type.GeneralTypeListener
     */
    void process(T instance) throws Exception;
}
