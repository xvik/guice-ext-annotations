package ru.vyarus.guice.ext.core.generator.impl;

import ru.vyarus.guice.ext.core.generator.GeneratorClassLoader;

/**
 * Default no-op initializer. Used if no specific implementation provided.
 *
 * @author Derric Gilling
 * @since 13.05.2016
 */
public class DefaultGeneratorClassLoader implements GeneratorClassLoader {

    @Override
    public ClassLoader getLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
