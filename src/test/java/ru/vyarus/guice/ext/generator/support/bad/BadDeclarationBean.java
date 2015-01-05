package ru.vyarus.guice.ext.generator.support.bad;

import com.google.inject.ProvidedBy;
import com.google.inject.internal.DynamicClassProvider;

import javax.inject.Singleton;

/**
 * Scope annotation can't be used directly on abstract type (guice doesn't allow it)
 * @author Vyacheslav Rusakov
 * @since 10.12.2014
 */
@Singleton
@ProvidedBy(DynamicClassProvider.class)
public abstract class BadDeclarationBean {
}
