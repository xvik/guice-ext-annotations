package ru.vyarus.guice.ext.core.generator;

import com.google.common.base.Preconditions;
import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.ProvidedBy;
import com.google.inject.internal.Annotations;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtNewConstructor;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Dynamically generates new class from abstract class or interface.
 * Resulted class may be used as implementation in guice.
 * <p>Guice will be able to apply aop to generated class methods. Annotations are copied from original class,
 * to let aop mechanisms work even for interfaces. As a result you may forget about class generation and think
 * of abstract class or interface as usual guice bean.</p>
 * <p>Abstract methods will not be implemented: abstract method call error will be thrown if you try to call it.
 * All abstract methods must be covered by guice aop.</p>
 * <p>It may be used directly in guice module to register interface or abstract class:
 * {@code bind(MyType.class).to(DynamicClassGenerator.generate(MyType.class))}.
 * Another option is to use {@link com.google.inject.internal.DynamicClassProvider}: annotate type with
 * {@code @ProvidedBy(DynamicClassProvider.class)} and either rely on JIT (don't register type at all) or
 * simply register type: @{code bind(MyType.class)}</p>
 * <p>Don't use scope annotations directly - instead wrap them into
 * {@link ru.vyarus.guice.ext.core.generator.ScopeAnnotation}, because guice doesn't allow scope definition on
 * abstract types.</p>
 *
 * @author Vyacheslav Rusakov
 * @see com.google.inject.internal.DynamicClassProvider
 * @since 10.12.2014
 */
public final class DynamicClassGenerator {

    /**
     * Postfix applied to interface or abstract class name to get generated class name.
     */
    public static final String DYNAMIC_CLASS_POSTFIX = "$GuiceDynamicClass";

    private DynamicClassGenerator() {
    }

    /**
     * Generates dynamic class, which guice may use as implementation and generate proxy above it,
     * correctly applying aop features.
     * <p>New class will inherit type annotations and constructor with annotations
     * (if base class use constructor injection). Also constructor inherits all annotations, including
     * parameters annotations.</p>
     *
     * @param type interface or abstract class
     * @param <T>  type
     * @return implementation class for provided type (will not generate if class already exist)
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> generate(final Class<T> type) {
        Preconditions.checkNotNull(type, "Original type required");
        Preconditions.checkArgument(type.isInterface() || Modifier.isAbstract(type.getModifiers()),
                "Type must be interface or abstract class, but provided type is not: %s", type.getName());
        try {
            final String targetClassName = type.getName() + DYNAMIC_CLASS_POSTFIX;
            final ClassPool pool = ClassPool.getDefault();
            Class targetClass;
            if (pool.getOrNull(targetClassName) == null) {
                // generating new class
                final CtClass impl = generateClass(targetClassName, type);
                targetClass = impl.toClass(type.getClassLoader(), null);
            } else {
                // class was already generated
                targetClass = Class.forName(targetClassName);
            }
            return (Class<T>) targetClass;
        } catch (Exception ex) {
            throw new DynamicClassException("Failed to generate class for " + type.getName(), ex);
        }
    }

    private static CtClass generateClass(final String targetClassName, final Class type) throws Exception {
        final ClassPool pool = ClassPool.getDefault();
        final CtClass ctType = pool.get(type.getName());
        CtClass impl;
        if (type.isInterface()) {
            impl = pool.makeClass(targetClassName);
            impl.addInterface(ctType);
        } else {
            impl = pool.makeClass(targetClassName, ctType);
            copyConstructor(impl, type);
        }
        impl.getClassFile().addAttribute(copyAnnotations(impl.getClassFile().getConstPool(), type));
        return impl;
    }

    private static void copyConstructor(final CtClass impl, final Class type) throws Exception {
        final Constructor ctor = findDIConstructor(type);
        if (ctor != null) {
            final CtConstructor ctConstructor = CtNewConstructor.make(
                    convertTypes(ctor.getParameterTypes()),
                    convertTypes(ctor.getExceptionTypes()),
                    CtNewConstructor.PASS_PARAMS, null, null, impl);
            final ConstPool constPool = impl.getClassFile().getConstPool();
            final MethodInfo methodInfo = ctConstructor.getMethodInfo();
            methodInfo.addAttribute(copyAnnotations(constPool, ctor));
            methodInfo.addAttribute(copyConstructorParametersAnnotations(constPool, ctor));
            impl.addConstructor(ctConstructor);
        }
    }

    private static Constructor findDIConstructor(final Class type) {
        Constructor target = null;
        for (Constructor ctor : type.getConstructors()) {
            if (ctor.isAnnotationPresent(Inject.class) || ctor.isAnnotationPresent(Inject.class)) {
                target = ctor;
                break;
            }
        }
        return target;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static ParameterAnnotationsAttribute copyConstructorParametersAnnotations(
            final ConstPool constPool, final Constructor ctor) throws Exception {
        final int count = ctor.getParameterTypes().length;
        final ParameterAnnotationsAttribute paramAnns = new ParameterAnnotationsAttribute(
                constPool, ParameterAnnotationsAttribute.visibleTag);
        final Annotation[][] paramAnnotations = new Annotation[count][];
        for (int i = 0; i < count; i++) {
            final java.lang.annotation.Annotation[] anns = ctor.getParameterAnnotations()[i];
            paramAnnotations[i] = new Annotation[anns.length];
            for (int j = 0; j < anns.length; j++) {
                paramAnnotations[i][j] = copyAnnotation(paramAnns.getConstPool(), anns[j]);
            }
        }
        paramAnns.setAnnotations(paramAnnotations);
        return paramAnns;
    }

    private static CtClass[] convertTypes(final Class<?>... types) throws Exception {
        final ClassPool pool = ClassPool.getDefault();
        final CtClass[] resTypes = new CtClass[types.length];
        for (int i = 0; i < resTypes.length; i++) {
            final Class<?> type = types[i];
            resTypes[i] = pool.get(type.getName());
        }
        return resTypes;
    }

    private static AnnotationsAttribute copyAnnotations(final ConstPool constPool,
                                                        final AnnotatedElement source) throws Exception {
        final AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        if (source.getAnnotations().length > 0) {

            for (java.lang.annotation.Annotation ann : source.getAnnotations()) {
                final Annotation annotation = processAnnotation(constPool, ann);
                if (annotation != null) {
                    attr.addAnnotation(annotation);
                }
            }
        }
        return attr;
    }

    private static Annotation processAnnotation(final ConstPool constPool,
                                                final java.lang.annotation.Annotation ann) throws Exception {
        Annotation res = null;
        // if we copy these annotation guice will go to infinite loop
        if (!(ann instanceof ProvidedBy || ann instanceof ImplementedBy)) {
            Preconditions.checkState(!Annotations.isScopeAnnotation(ann.annotationType()),
                    "Don't use scope annotations directly - use @ScopeAnnotation(TargetScope) wrapper, "
                            + "because guice doesn't allow scope annotations on abstract types");
            if (ann instanceof ScopeAnnotation) {
                res = new Annotation(constPool,
                        ClassPool.getDefault().get(((ScopeAnnotation) ann).value().getName()));
            } else {
                res = copyAnnotation(constPool, ann);
            }
        }
        return res;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static Annotation copyAnnotation(final ConstPool constPool,
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
}
