package ru.vyarus.guice.ext.core.util;

import java.lang.reflect.Method;

/**
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public final class Utils {

    private Utils() {
    }

    public static boolean isPackageValid(final Class type) {
        // JDK proxies of public interfaces have no package
        final Package packaj = type.getPackage();
        return !(packaj == null || packaj.getName().startsWith("java"));
    }

    public static void checkNoParams(Method method) {
        if (method.getParameterTypes().length > 0)
            throw new IllegalStateException("Method without parameters required");
    }
}
