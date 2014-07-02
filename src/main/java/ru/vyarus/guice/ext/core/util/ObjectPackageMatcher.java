package ru.vyarus.guice.ext.core.util;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;

/**
 * Object class matcher.
 * Useful to limit post processors appliance scope by specific package (and sub packages)
 *
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public class ObjectPackageMatcher<T> extends AbstractMatcher<T> {
    private String pkg;

    public ObjectPackageMatcher(final String pkg) {
        this.pkg = pkg;
    }

    @Override
    public boolean matches(final T o) {
        Class<?> type = o instanceof TypeLiteral ? ((TypeLiteral) o).getRawType() : o.getClass();
        return Utils.isPackageValid(type) && type.getPackage().getName().startsWith(pkg);
    }
}