package ru.vyarus.guice.ext.generator.support.bad;

import com.google.inject.ProvidedBy;
import com.google.inject.Singleton;
import com.google.inject.internal.DynamicSingletonProvider;
import ru.vyarus.guice.ext.core.generator.ScopeAnnotation;

/**
 * Singleton declaration duplication not allowed
 *
 * @author Vyacheslav Rusakov
 * @since 06.01.2015
 */
@ScopeAnnotation(Singleton.class)
@ProvidedBy(DynamicSingletonProvider.class)
public interface BadSingletonDeclaration {
}
