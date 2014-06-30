package ru.vyarus.guice.ext.managed;

import ru.vyarus.guice.ext.core.method.MethodPostProcessor;
import ru.vyarus.guice.ext.core.util.Utils;
import ru.vyarus.guice.ext.managed.destroyable.AnnotatedMethodDestroyable;
import ru.vyarus.guice.ext.managed.destroyable.DestroyableManager;

import javax.annotation.PreDestroy;
import java.lang.reflect.Method;

/**
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public class PreDestroyAnnotationProcessor implements MethodPostProcessor<PreDestroy> {

    private DestroyableManager manager;

    public PreDestroyAnnotationProcessor(DestroyableManager manager) {
        this.manager = manager;
    }

    @Override
    public void process(PreDestroy annotation, Method method, Object instance) throws Exception {
        Utils.checkNoParams(method);
        manager.register(new AnnotatedMethodDestroyable(method, instance));
    }
}
