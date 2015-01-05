package ru.vyarus.guice.ext.postprocess;

import com.google.inject.*;
import com.google.inject.matcher.Matchers;
import org.junit.Assert;
import org.junit.Test;
import ru.vyarus.guice.ext.core.type.GeneralTypeListener;
import ru.vyarus.guice.ext.postprocess.support.*;

/**
 * @author Vyacheslav Rusakov
 * @since 06.01.2015
 */
public class TypePostProcessorTest {

    @Test
    public void testPostProcessingByType() throws Exception {
        final PostProcessor postProcessor = new PostProcessor();
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Bean1.class).asEagerSingleton();
                bind(Bean2.class).asEagerSingleton();
                bind(Bean3.class).asEagerSingleton();
                bindListener(Matchers.any(),
                        new GeneralTypeListener<AbstractBean>(AbstractBean.class, postProcessor));
            }
        });
        Assert.assertEquals(postProcessor.called, 2);
        Assert.assertEquals(injector.getInstance(Bean1.class).called, 1);
        Assert.assertEquals(injector.getInstance(Bean2.class).called, 1);
    }

    @Test(expected = CreationException.class)
    public void testPostProcessingFailure() throws Exception {
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ExceptionalBean.class).asEagerSingleton();
                bindListener(Matchers.any(),
                        new GeneralTypeListener<AbstractBean>(AbstractBean.class, new PostProcessor()));
            }
        });
    }

    @Test(expected = ProvisionException.class)
    public void testPostProcessingFailure2() throws Exception {
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ExceptionalBean.class).in(Singleton.class);
                bindListener(Matchers.any(),
                        new GeneralTypeListener<AbstractBean>(AbstractBean.class, new PostProcessor()));
            }
        }).getInstance(ExceptionalBean.class);
    }
}
