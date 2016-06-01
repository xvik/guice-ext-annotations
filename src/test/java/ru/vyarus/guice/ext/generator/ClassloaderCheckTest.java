package ru.vyarus.guice.ext.generator;

import org.junit.Assert;
import org.junit.Test;
import ru.vyarus.guice.ext.core.generator.DynamicClassGenerator;
import ru.vyarus.guice.ext.generator.support.classloader.SampleBean;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Vyacheslav Rusakov
 * @since 01.06.2016
 */
public class ClassloaderCheckTest {

    @Test
    public void checkClassLoaderBinding() throws Exception {
        // custom classloader used to load classes (parent implicitly set to ext instead of system
        // to avoid classpath entry load)
        final URL[] classpath = {ClassloaderCheckTest.class.getResource("/")};
        final ClassLoader extCL = ClassLoader.getSystemClassLoader().getParent();
        ClassLoader cl = new URLClassLoader(classpath, extCL);

        // loading abstract type
        final String typeName = SampleBean.class.getName();
        Class<?> type = cl.loadClass(typeName);
        Assert.assertEquals(cl, type.getClassLoader());

        // generated class must be assigned to custom classloader
        Class<?> generated =  DynamicClassGenerator.generate(type);
        Assert.assertEquals(cl, generated.getClassLoader());

        // now change classloader to make sure class will be re-generated (correct javassist usage)
        ClassLoader cl2 = new URLClassLoader(classpath, extCL);

        // loading abstract type again with different cl
        Class<?> type2 = cl2.loadClass(typeName);
        Assert.assertEquals(cl2, type2.getClassLoader());

        // generated class must be assigned to new custom classloader
        Class<?> generated2 =  DynamicClassGenerator.generate(type2);
        Assert.assertEquals(cl2, generated2.getClassLoader());
    }
}
