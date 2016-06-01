package ru.vyarus.guice.ext.core.generator;

import com.google.common.collect.ImmutableMap;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.*;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Sets annotation method value.
 * <p>Based on code from <a href="http://tapestry.apache.org/">tapestry 5</a>.</p>
 *
 * @author Vyacheslav Rusakov
 * @since 08.12.2014
 */
@SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes", "PMD.AvoidUsingShortType"})
public class AnnotationMemberValueVisitor implements MemberValueVisitor {

    private static final Map<Class<?>, CtClass> PRIMITIVES = ImmutableMap.<Class<?>, CtClass>builder()
            .put(boolean.class, CtClass.booleanType)
            .put(Boolean.class, CtClass.booleanType)
            .put(byte.class, CtClass.byteType)
            .put(Byte.class, CtClass.byteType)
            .put(char.class, CtClass.charType)
            .put(Character.class, CtClass.charType)
            .put(short.class, CtClass.shortType)
            .put(Short.class, CtClass.shortType)
            .put(int.class, CtClass.intType)
            .put(Integer.class, CtClass.intType)
            .put(long.class, CtClass.longType)
            .put(Long.class, CtClass.longType)
            .put(float.class, CtClass.floatType)
            .put(Float.class, CtClass.floatType)
            .put(double.class, CtClass.doubleType)
            .put(Double.class, CtClass.doubleType)
            .build();

    private final ClassPool classPool;
    private final ConstPool constPool;
    private final Object value;

    public AnnotationMemberValueVisitor(final ClassPool classPool, final ConstPool constPool, final Object value) {
        this.classPool = classPool;
        this.constPool = constPool;
        this.value = value;
    }

    public void visitAnnotationMemberValue(final AnnotationMemberValue mb) {
        final Class<?> annotationType = getClass(value);
        final Method[] methods = annotationType.getDeclaredMethods();

        try {
            for (final Method method : methods) {
                final Object result = method.invoke(value);
                mb.getValue().addMemberValue(method.getName(), createValue(result));

            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy annotation value", e);
        }
    }

    public void visitArrayMemberValue(final ArrayMemberValue mb) {
        final int length = Array.getLength(this.value);
        final MemberValue[] members = new MemberValue[length];
        try {
            for (int i = 0; i < length; i++) {
                final Object object = Array.get(this.value, i);
                members[i] = createValue(object);
            }
            mb.setValue(members);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to copy array value", e);
        }
    }

    public void visitBooleanMemberValue(final BooleanMemberValue mb) {
        mb.setValue((Boolean) this.value);
    }

    public void visitByteMemberValue(final ByteMemberValue mb) {
        mb.setValue((Byte) this.value);
    }

    public void visitCharMemberValue(final CharMemberValue mb) {
        mb.setValue((Character) this.value);
    }

    public void visitDoubleMemberValue(final DoubleMemberValue mb) {
        mb.setValue((Double) this.value);
    }

    public void visitEnumMemberValue(final EnumMemberValue mb) {
        final Enum<?> enumeration = (Enum<?>) this.value;
        final Class type = enumeration.getDeclaringClass();
        mb.setType(type.getName());
        mb.setValue(enumeration.name());
    }

    public void visitFloatMemberValue(final FloatMemberValue mb) {
        mb.setValue((Float) this.value);
    }

    public void visitIntegerMemberValue(final IntegerMemberValue mb) {
        mb.setValue((Integer) this.value);
    }

    public void visitLongMemberValue(final LongMemberValue mb) {
        mb.setValue((Long) this.value);
    }

    public void visitShortMemberValue(final ShortMemberValue mb) {
        mb.setValue((Short) this.value);
    }

    public void visitStringMemberValue(final StringMemberValue mb) {
        mb.setValue((String) this.value);
    }

    public void visitClassMemberValue(final ClassMemberValue mb) {
        mb.setValue(((Class) this.value).getName());
    }

    private MemberValue createValue(final Object value) throws Exception {
        final MemberValue memberValue = Annotation.createMemberValue(
                this.constPool, getCtClass(classPool, getClass(value)));
        memberValue.accept(new AnnotationMemberValueVisitor(this.classPool, this.constPool, value));
        return memberValue;
    }

    private Class<?> getClass(final Object object) {
        final boolean isAnnotation = object instanceof java.lang.annotation.Annotation;
        return isAnnotation ? ((java.lang.annotation.Annotation) object).annotationType() : object.getClass();
    }

    private static CtClass getCtClass(final ClassPool pool, final Class<?> type) throws Exception {
        return PRIMITIVES.containsKey(type)
                ? PRIMITIVES.get(type)
                : pool.get(type.getName());
    }
}
