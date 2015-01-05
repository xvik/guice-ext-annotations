package ru.vyarus.guice.ext.postprocess.support;

/**
 * @author Vyacheslav Rusakov
 * @since 06.01.2015
 */
public abstract class AbstractBean {
    public int called;

    public void call() {
        called++;
    }
}
