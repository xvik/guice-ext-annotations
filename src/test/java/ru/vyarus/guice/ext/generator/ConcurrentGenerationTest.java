package ru.vyarus.guice.ext.generator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.vyarus.guice.ext.core.generator.DynamicClassGenerator;
import ru.vyarus.guice.ext.generator.support.AbstractBean;
import ru.vyarus.guice.ext.generator.support.InterfaceBean;
import ru.vyarus.guice.ext.generator.support.ProvidedInterfaceBean;
import ru.vyarus.guice.ext.generator.support.ctor.CustomConstructorBean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Vyacheslav Rusakov
 * @since 02.06.2016
 */
public class ConcurrentGenerationTest {

    ExecutorService executor;

    @Before
    public void setUp() throws Exception {
        executor = Executors.newFixedThreadPool(20);
    }

    @After
    public void tearDown() throws Exception {
        executor.shutdown();
    }

    @Test
    public void testConcurrency() throws Exception {
        List<Future<?>> executed = new ArrayList<Future<?>>();
        final Class[] types = new Class[]{
                AbstractBean.class,
                InterfaceBean.class,
                ProvidedInterfaceBean.class,
                CustomConstructorBean.class
        };
        int count = 20;
        for (int i = 0; i < count; i++) {
            executed.add(
                    executor.submit(new Runnable() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public void run() {
                            DynamicClassGenerator.generate(types[(int)(types.length*Math.random())]);
                        }
                    })
            );
        }
        for(Future<?> future: executed) {
            future.get();
        }
    }

}
