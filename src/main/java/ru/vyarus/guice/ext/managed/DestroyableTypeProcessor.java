package ru.vyarus.guice.ext.managed;

import ru.vyarus.guice.ext.core.type.TypePostProcessor;
import ru.vyarus.guice.ext.managed.destroyable.Destroyable;
import ru.vyarus.guice.ext.managed.destroyable.DestroyableManager;

/**
 * Registers beans implementing {@code Destroyable} interface to {@code DestroyableManager} to be executed on shutdown.
 *
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public class DestroyableTypeProcessor implements TypePostProcessor<Destroyable> {
    private final DestroyableManager manager;

    public DestroyableTypeProcessor(final DestroyableManager manager) {
        this.manager = manager;
    }

    @Override
    public void process(final Destroyable instance) {
        manager.register(instance);
    }
}
