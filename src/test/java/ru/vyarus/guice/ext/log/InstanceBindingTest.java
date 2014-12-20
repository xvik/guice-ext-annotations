package ru.vyarus.guice.ext.log;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import org.junit.Test;
import ru.vyarus.guice.ext.ExtAnnotationsModule;

import static org.junit.Assert.assertNotNull;

/**
 * @author Vyacheslav Rusakov
 * @since 20.12.2014
 */
public class InstanceBindingTest {

    @Test
    public void testInstanceBinding() throws Exception {
        LogTest.OkBean bean = Guice.createInjector(new ExtAnnotationsModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bind(LogTest.OkBean.class).toInstance(new LogTest.OkBean());
            }
        }).getInstance(LogTest.OkBean.class);
        assertNotNull(bean.logger);
    }
}
