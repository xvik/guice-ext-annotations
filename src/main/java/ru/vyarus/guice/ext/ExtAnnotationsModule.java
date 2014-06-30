package ru.vyarus.guice.ext;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import ru.vyarus.guice.ext.core.field.AnnotatedFieldTypeListener;
import ru.vyarus.guice.ext.core.method.AnnotatedMethodTypeListener;
import ru.vyarus.guice.ext.core.type.GeneralTypeListener;
import ru.vyarus.guice.ext.core.util.ObjectPackageMatcher;
import ru.vyarus.guice.ext.log.Log;
import ru.vyarus.guice.ext.log.Slf4jLogAnnotationProcessor;
import ru.vyarus.guice.ext.managed.DestroyableTypeProcessor;
import ru.vyarus.guice.ext.managed.PostConstructAnnotationProcessor;
import ru.vyarus.guice.ext.managed.PreDestroyAnnotationProcessor;
import ru.vyarus.guice.ext.managed.destroyable.Destroyable;
import ru.vyarus.guice.ext.managed.destroyable.DestroyableManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author Vyacheslav Rusakov
 * @since 29.06.2014
 */
public class ExtAnnotationsModule extends AbstractModule {

    private Matcher<Object> typeMatcher;

    public ExtAnnotationsModule() {
        this(Matchers.any());
    }

    public ExtAnnotationsModule(final String pkg) {
        this(new ObjectPackageMatcher<Object>(pkg));
    }

    public ExtAnnotationsModule(Matcher<Object> typeMatcher) {
        this.typeMatcher = typeMatcher;
    }

    @Override
    protected void configure() {
        DestroyableManager manager = configureManager(new DestroyableManager());

        bindListener(typeMatcher, new GeneralTypeListener<Destroyable>(
                Destroyable.class, new DestroyableTypeProcessor(manager)));

        bindListener(typeMatcher, new AnnotatedMethodTypeListener<PostConstruct>(
                PostConstruct.class, new PostConstructAnnotationProcessor()));

        bindListener(typeMatcher, new AnnotatedMethodTypeListener<PreDestroy>(
                PreDestroy.class, new PreDestroyAnnotationProcessor(manager)));

        bindListener(typeMatcher, new AnnotatedFieldTypeListener<Log>(
                Log.class, new Slf4jLogAnnotationProcessor()));
    }


    protected DestroyableManager configureManager(DestroyableManager manager) {
        bind(DestroyableManager.class).toInstance(manager);
        // if logic will not call destroy at least it will be called before jvm shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(manager));
        return manager;
    }
}
