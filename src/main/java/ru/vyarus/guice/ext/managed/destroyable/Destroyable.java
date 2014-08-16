package ru.vyarus.guice.ext.managed.destroyable;

/**
 * Marker interface for beans which require some finalization logic (@PostConstruct alternative).
 *
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public interface Destroyable {

    /**
     * Called on context shutdown (by default on jvm shutdown), but may be called manually through destroy manager
     * {@code ru.vyarus.guice.ext.managed.destroyable.DestroyableManager#destroy()}.
     * Will be called one time no matter how many time destroy will be asked by manager.
     * It is safe to avoid explicit exception handling (except special cases required by logic).
     *
     * @throws Exception  on any unrecoverable error
     */
    void preDestroy() throws Exception;
}
