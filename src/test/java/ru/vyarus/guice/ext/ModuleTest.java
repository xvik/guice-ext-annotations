package ru.vyarus.guice.ext;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;
import org.slf4j.Logger;
import ru.vyarus.guice.ext.log.Log;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public class ModuleTest {

    @Test
    public void testWrongPkg() throws Exception {
        Injector injector = Guice.createInjector(new ExtAnnotationsModule("wrong.package"));
        Bean bean = injector.getInstance(Bean.class);
        assertNull(bean.logger);
    }

    @Test
    public void testGoodPkg() throws Exception {
        Injector injector = Guice.createInjector(new ExtAnnotationsModule(Bean.class.getPackage().getName()));
        Bean bean = injector.getInstance(Bean.class);
        assertNotNull(bean.logger);
    }

    public static class Bean {
        @Log
        Logger logger;
    }
}
