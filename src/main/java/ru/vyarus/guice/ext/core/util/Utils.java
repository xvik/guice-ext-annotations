package ru.vyarus.guice.ext.core.util;

import java.lang.reflect.Method;

/**
 * Generic utilities.
 *
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public final class Utils {

    private Utils() {
    }

    /**
     * Important check, because JDK proxies of public interfaces have no package
     * (thanks to @binkley https://github.com/99soft/lifegycle/pull/5).
     *
     * @param type class type to check
     * @return true if package could be resolved, false otherwise
     */
    public static boolean isPackageValid(final Class type) {
        boolean res = false;
        if (type != null) {
            final Package packaj = type.getPackage();
            res = !(packaj == null || packaj.getName().startsWith("java"));
        }
        return res;
    }

    /**
     * Checks that method has no parameters, otherwise throws exception.
     *
     * @param method method to check
     */
    public static void checkNoParams(final Method method) {
        if (method.getParameterTypes().length > 0) {
            throw new IllegalStateException("Method without parameters required");
        }
    }
}
