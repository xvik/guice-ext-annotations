package ru.vyarus.guice.ext.postprocess.support;

/**
 * @author Vyacheslav Rusakov
 * @since 06.01.2015
 */
public class ExceptionalBean extends AbstractBean{

    @Override
    public void call() {
        throw new IllegalStateException("Bad");
    }
}
