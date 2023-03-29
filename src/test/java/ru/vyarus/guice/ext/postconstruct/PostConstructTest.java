package ru.vyarus.guice.ext.postconstruct;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import jakarta.annotation.PostConstruct;
import org.junit.Before;
import org.junit.Test;
import ru.vyarus.guice.ext.ExtAnnotationsModule;

import static org.junit.Assert.assertEquals;

/**
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public class PostConstructTest {
    Injector injector;

    @Before
    public void setUp() throws Exception {
        injector = Guice.createInjector(new ExtAnnotationsModule());
    }

    @Test
    public void testSuccess() throws Exception {
        OkBean bean = injector.getInstance(OkBean.class);
        assertEquals(2, bean.counter);
    }

    @Test(expected = ProvisionException.class)
    public void testFail() throws Exception {
        injector.getInstance(KoBean.class);
    }

    @Test(expected = ProvisionException.class)
    public void testExceptionFail() throws Exception {
        injector.getInstance(KoExceptionBean.class);
    }

    public static class OkBean {
        private int counter;

        @PostConstruct
        public void init() {
            counter++;
        }

        @PostConstruct
        private void init2() {
            counter++;
        }
    }

    public static class KoBean {
        @PostConstruct
        public void init(Object smth) {
        }
    }

    public static class KoExceptionBean {
        @PostConstruct
        public void init() {
            throw new IllegalStateException("foo");
        }
    }
}
