package ru.vyarus.guice.ext.predestroy;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;
import ru.vyarus.guice.ext.ExtAnnotationsModule;
import ru.vyarus.guice.ext.managed.destroyable.Destroyable;
import ru.vyarus.guice.ext.managed.destroyable.DestroyableManager;

import static org.junit.Assert.assertEquals;

/**
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public class DestroyableTest {
    Injector injector;
    DestroyableManager manager;

    @Before
    public void setUp() throws Exception {
        injector = Guice.createInjector(new ExtAnnotationsModule());
        manager = injector.getInstance(DestroyableManager.class);
    }

    @Test
    public void testCall() throws Exception {
        Bean bean = injector.getInstance(Bean.class);
        manager.destroy();
        assertEquals(1, bean.counter);

    }

    public static class Bean implements Destroyable {
        int counter;

        @Override
        public void preDestroy() throws Exception {
            counter++;
        }
    }
}
