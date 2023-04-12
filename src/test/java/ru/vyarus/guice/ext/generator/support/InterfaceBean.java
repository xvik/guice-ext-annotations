package ru.vyarus.guice.ext.generator.support;

import ru.vyarus.guice.ext.core.generator.ScopeAnnotation;
import ru.vyarus.guice.ext.generator.support.aop.CustomAop;

import com.google.inject.Singleton;
import java.lang.annotation.ElementType;

/**
 * @author Vyacheslav Rusakov
 * @since 10.12.2014
 */
@AnnotationCheck(
        ann = @ScopeAnnotation(Singleton.class),
        arr = {1,2,3},
        arrBool = {true, false},
        arrByte = {1, 2},
        arrChar = {',', 'l'},
        arrDbl = {0.1, 0.2},
        arrFlt = {0.1f, 0.2f},
        arrLng = {1l, 2l},
        arrShrt = {1,2},
        arrObj = {"tst", "tst2"},
        bool = true,
        bte = 1,
        chr = ',',
        dbl = 1d,
        integer = 1,
        lng = 1l,
        flt = 1,
        srt = 1,
        str = "string",
        cls = InterfaceBean.class,
        enm = ElementType.TYPE
)
public interface InterfaceBean {

    @CustomAop
    String hello();
}
