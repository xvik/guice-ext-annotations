package ru.vyarus.guice.ext.generator.support.bad;

import com.google.inject.ProvidedBy;
import com.google.inject.internal.DynamicClassProvider;

/**
 * Class generation can't be used with non abstract types
 *
 * @author Vyacheslav Rusakov
 * @since 10.12.2014
 */
@ProvidedBy(DynamicClassProvider.class)
public class WrongUsageBean {
}
