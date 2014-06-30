package ru.vyarus.guice.ext.log;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import ru.vyarus.guice.ext.ExtAnnotationsModule;

import static org.junit.Assert.*;

/**
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public class LogTest {
    Injector injector;

    @Before
    public void setUp() throws Exception {
        injector = Guice.createInjector(new ExtAnnotationsModule());
    }

    @Test
    public void testSuccess() throws Exception {
        OkBean bean = injector.getInstance(OkBean.class);
        assertNotNull(bean.logger);
        assertNotNull(bean.logger2);
    }

    @Test(expected = ProvisionException.class)
    public void testFail() throws Exception {
        injector.getInstance(KoBean.class);
    }

    public static class OkBean {
        @Log
        public Logger logger;
        @Log
        private Logger logger2;
    }

    public static class KoBean {
        @Log
        private java.util.logging.Logger logger2;
    }
}
