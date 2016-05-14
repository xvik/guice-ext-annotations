package ru.vyarus.guice.ext.core.generator;

import com.google.inject.ImplementedBy;
import ru.vyarus.guice.ext.core.generator.impl.DefaultGeneratorClassLoader;

/**
 * <p>Initialize a custom classloader. Useful when application is running in separate context</p>
 *
 * @author Derric Gilling
 * @since 30.01.2015
 */
@ImplementedBy(DefaultGeneratorClassLoader.class)
public interface GeneratorClassLoader {

    ClassLoader getLoader();
}
