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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Additional annotations support module: @PostConstruct, @PreDestroy, @Log.
 *
 * @author Vyacheslav Rusakov
 * @since 29.06.2014
 */
@SuppressWarnings("checkstyle:ClassDataAbstractionCoupling")
public class ExtAnnotationsModule extends AbstractModule {

    private Matcher<Object> typeMatcher;

    /**
     * Default module constructor to check annotations on all beans.
     */
    public ExtAnnotationsModule() {
        this(Matchers.any());
    }

    /**
     * Constructs annotation module with annotation scan limited to provided package.
     * (used mainly for startup performance optimization)
     *
     * @param pkg package to limit beans, where annotations processed
     */
    public ExtAnnotationsModule(final String pkg) {
        this(new ObjectPackageMatcher<>(pkg));
    }

    /**
     * Constructs annotation module with custom bean matcher for annotations processing.
     *
     * @param typeMatcher matcher to select beans for annotations processing
     */
    public ExtAnnotationsModule(final Matcher<Object> typeMatcher) {
        this.typeMatcher = typeMatcher;
    }

    @Override
    protected void configure() {
        final DestroyableManager manager = configureManager(new DestroyableManager());

        bindListener(typeMatcher, new GeneralTypeListener<>(
                Destroyable.class, new DestroyableTypeProcessor(manager)));

        bindListener(typeMatcher, new AnnotatedMethodTypeListener<>(
                PostConstruct.class, new PostConstructAnnotationProcessor()));

        bindListener(typeMatcher, new AnnotatedMethodTypeListener<>(
                PreDestroy.class, new PreDestroyAnnotationProcessor(manager)));

        bindListener(typeMatcher, new AnnotatedFieldTypeListener<>(
                Log.class, new Slf4jLogAnnotationProcessor()));
    }


    /**
     * Registers destroyable manager in injector and adds shutdown hook to process destroy on jvm shutdown.
     *
     * @param manager destroyable manager instance
     * @return manager instance
     */
    protected DestroyableManager configureManager(final DestroyableManager manager) {
        bind(DestroyableManager.class).toInstance(manager);
        // if logic will not call destroy at least it will be called before jvm shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(manager));
        return manager;
    }
}
