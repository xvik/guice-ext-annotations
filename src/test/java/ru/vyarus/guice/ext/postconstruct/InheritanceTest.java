package ru.vyarus.guice.ext.postconstruct;

import com.google.inject.Guice;
import org.junit.Assert;
import org.junit.Test;
import ru.vyarus.guice.ext.ExtAnnotationsModule;

import javax.annotation.PostConstruct;

/**
 * @author Vyacheslav Rusakov
 * @since 20.12.2014
 */
public class InheritanceTest {

    @Test
    public void testInheritance() throws Exception {
        Bean bean = Guice.createInjector(new ExtAnnotationsModule()).getInstance(Bean.class);
        Assert.assertEquals(2, bean.counter);
    }

    public static class Bean extends BaseBean {

    }

    public static abstract class BaseBean {
        int counter;

        @PostConstruct
        public void init() {
            counter++;
        }

        @PostConstruct
        private void init2() {
            counter++;
        }
    }
}
