package ru.vyarus.guice.ext.generator.support.bad;

import com.google.inject.ProvidedBy;
import com.google.inject.internal.DynamicClassProvider;

import javax.inject.Singleton;

/**
 * @author Vyacheslav Rusakov
 * @since 10.12.2014
 */
@Singleton
@ProvidedBy(DynamicClassProvider.class)
public abstract class BadDeclarationBean {
}
