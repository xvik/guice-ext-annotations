package ru.vyarus.guice.ext.postprocess.support;

import ru.vyarus.guice.ext.core.type.TypePostProcessor;

/**
 * @author Vyacheslav Rusakov
 * @since 06.01.2015
 */
public class PostProcessor implements TypePostProcessor<AbstractBean> {

    public int called;

    @Override
    public void process(AbstractBean instance) throws Exception {
        instance.call();
        called++;
    }
}
