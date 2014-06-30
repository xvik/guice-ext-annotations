package ru.vyarus.guice.ext.managed.destroyable;

/**
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public interface Destroyable {

    void preDestroy() throws Exception;
}
