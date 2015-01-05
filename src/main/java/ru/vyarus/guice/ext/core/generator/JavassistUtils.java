package ru.vyarus.guice.ext.core.generator;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;

import java.lang.reflect.Method;

/**
 * Helper javassist methods.
 *
 * @author Vyacheslav Rusakov
 * @since 06.01.2015
 */
public final class JavassistUtils {

    private JavassistUtils() {
    }

    /**
     * @param constPool constants pool
     * @param ann       annotation to copy
     * @return javassist annotation object (copy of original annotation)
     * @throws Exception on errors
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public static Annotation copyAnnotation(final ConstPool constPool,
                                            final java.lang.annotation.Annotation ann) throws Exception {
        final ClassPool pool = ClassPool.getDefault();
        final Class<? extends java.lang.annotation.Annotation> annotationType = ann.annotationType();
        final Annotation copy = new Annotation(annotationType.getName(), constPool);
        final Method[] methods = annotationType.getDeclaredMethods();
        for (final Method method : methods) {
            final CtClass ctType = pool.get(method.getReturnType().getName());
            final MemberValue memberValue = Annotation.createMemberValue(constPool, ctType);
            final Object value = method.invoke(ann);
            memberValue.accept(new AnnotationMemberValueVisitor(constPool, value));
            copy.addMemberValue(method.getName(), memberValue);
        }
        return copy;
    }

    /**
     * @param types java types
     * @return array of javassist types
     * @throws Exception on errors
     */
    public static CtClass[] convertTypes(final Class<?>... types) throws Exception {
        final ClassPool pool = ClassPool.getDefault();
        final CtClass[] resTypes = new CtClass[types.length];
        for (int i = 0; i < resTypes.length; i++) {
            final Class<?> type = types[i];
            resTypes[i] = pool.get(type.getName());
        }
        return resTypes;
    }
}
