package ru.vyarus.guice.ext.managed;

import ru.vyarus.guice.ext.core.method.MethodPostProcessor;
import ru.vyarus.guice.ext.core.util.Utils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;

/**
 * Process bean @PostConstruct annotated methods: executes annotated method just after bean initialization.
 *
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public class PostConstructAnnotationProcessor implements MethodPostProcessor<PostConstruct> {

    @Override
    public void process(final PostConstruct annotation, final Method method, final Object instance) throws Exception {
        Utils.checkNoParams(method);
        method.invoke(instance);
    }
}
