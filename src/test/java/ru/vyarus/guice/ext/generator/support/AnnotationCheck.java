package ru.vyarus.guice.ext.generator.support;

import ru.vyarus.guice.ext.core.generator.ScopeAnnotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Vyacheslav Rusakov
 * @since 10.12.2014
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface AnnotationCheck {
    ScopeAnnotation ann();
    int[] arr();
    boolean[] arrBool();
    short[] arrShrt();
    long[] arrLng();
    byte[] arrByte();
    double[] arrDbl();
    float[] arrFlt();
    char[] arrChar();
    String[] arrObj();
    boolean bool();
    byte bte();
    char chr();
    double dbl();
    int integer();
    long lng();
    float flt();
    short srt();
    String str();
    Class cls();
    ElementType enm();
}
