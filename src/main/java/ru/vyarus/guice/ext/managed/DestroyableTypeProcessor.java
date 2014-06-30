package ru.vyarus.guice.ext.managed;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import ru.vyarus.guice.ext.core.type.TypePostProcessor;
import ru.vyarus.guice.ext.managed.destroyable.Destroyable;
import ru.vyarus.guice.ext.managed.destroyable.DestroyableManager;

/**
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public class DestroyableTypeProcessor implements TypePostProcessor<Destroyable> {
    private DestroyableManager manager;

    public DestroyableTypeProcessor(DestroyableManager manager) {
        this.manager = manager;
    }

    @Override
    public void process(Destroyable instance) {
        manager.register(instance);
    }
}
