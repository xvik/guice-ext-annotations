package ru.vyarus.guice.ext.managed.destroyable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Manage destroyable instances.
 * Implements {@code Runnable} to be used as shutdown hook.
 *
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public class DestroyableManager implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(DestroyableManager.class);

    private final List<Destroyable> destroyListeners = new ArrayList<Destroyable>();

    /**
     * Register destroyable instance to be called on context shutdown.
     * Not thread safe (assuming single thread injector initialization)
     *
     * @param destroyable destroyable instance
     * @see ru.vyarus.guice.ext.managed.PostConstructAnnotationProcessor regsters annotated methods
     * @see ru.vyarus.guice.ext.managed.DestroyableTypeProcessor registers beans anootated with {@code Destroyable}
     */
    public void register(final Destroyable destroyable) {
        // assuming single thread injector creation
        destroyListeners.add(destroyable);
    }

    /**
     * Called on context shutdown to call all registered destroyable instances.
     * By default called on jvm shutdown, but may be called manually to synchronise with
     * some other container shutdown (e.g. web container)
     * Safe to call many times, but all destroy instances will be processed only on first call.
     * Thread safe.
     */
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
