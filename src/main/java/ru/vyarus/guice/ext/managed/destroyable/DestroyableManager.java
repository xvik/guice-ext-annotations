package ru.vyarus.guice.ext.managed.destroyable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public class DestroyableManager implements Runnable{
    private Logger logger = LoggerFactory.getLogger(DestroyableManager.class);

    private List<Destroyable> destroyListeners = new ArrayList<Destroyable>();

    public void register(Destroyable destroyable) {
        // assuming single thread injector creation
        destroyListeners.add(destroyable);
    }

    public void destroy() {
        // just for the case
        synchronized (this) {
            for (Destroyable destroyable : destroyListeners) {
                try {
                    destroyable.preDestroy();
                } catch (Exception ex) {
                    logger.error("Failed to properly destroy bean", ex);
                }
            }
            destroyListeners.clear();
        }
    }

    @Override
    public void run() {
        destroy();
    }
}
