package ru.vyarus.guice.ext.core.util;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;

/**
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public class ObjectPackageMatcher<T> extends AbstractMatcher<T> {
    private String pkg;

    public ObjectPackageMatcher(String pkg) {
        this.pkg = pkg;
    }

    @Override
    public boolean matches(T o) {
        Class<?> type = o instanceof TypeLiteral ? ((TypeLiteral) o).getRawType() : o.getClass();
        return Utils.isPackageValid(type) && type.getPackage().getName().startsWith(pkg);
    }
}