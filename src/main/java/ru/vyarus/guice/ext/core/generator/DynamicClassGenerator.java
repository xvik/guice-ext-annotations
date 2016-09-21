package ru.vyarus.guice.ext.core.generator;

import com.google.common.base.Preconditions;
import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.ProvidedBy;
import com.google.inject.internal.Annotations;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.Annotation;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * Dynamically generates new class from abstract class or interface.
 * Resulted class may be used as implementation in guice.
 * Generated class will be tied to class loader of original type.
 * It is safe to to use with dynamic class loaders (like play dev mode).
 * <p>Guice will be able to apply aop to generated class methods. Annotations are copied from original class,
 * to let aop mechanisms work even for interfaces. As a result you may forget about class generation and think
 * of abstract class or interface as usual guice bean.</p>
 * <p>Abstract methods will not be implemented: abstract method call error will be thrown if you try to call it.
 * All abstract methods must be covered by guice aop.</p>
 * <p>It may be used directly in guice module to register interface or abstract class:
 * {@code bind(MyType.class).to(DynamicClassGenerator.generate(MyType.class))}.
 * Another option is to use {@link com.google.inject.internal.DynamicClassProvider}: annotate type with
 * {@code @ProvidedBy(DynamicClassProvider.class)} and either rely on JIT (don't register type at all) or
 * simply register type: @{code bind(MyType.class)}.
 * If used with injectors hierarchy or within private modules, use "anchor" dependency to prevent bubbling up
 * for resulted binding (see for example anchor implementation for use with {@code @ProvidedBy} annotation
 * {@link ru.vyarus.guice.ext.core.generator.anchor.GeneratorAnchorModule}).</p>
 * <p>Don't use scope annotations directly - instead wrap them into
 * {@link ru.vyarus.guice.ext.core.generator.ScopeAnnotation}, because guice doesn't allow scope definition on
 * abstract types.</p>
 *
 * @author Vyacheslav Rusakov
 * @see com.google.inject.internal.DynamicClassProvider
 * @since 10.12.2014
 */
@SuppressWarnings("checkstyle:ClassDataAbstractionCoupling")
public final class DynamicClassGenerator {

    /**
     * Postfix applied to interface or abstract class name to get generated class name.
     */
    public static final String DYNAMIC_CLASS_POSTFIX = "$GuiceDynamicClass";

    private DynamicClassGenerator() {
    }

    /**
     * Shortcut for {@link #generate(Class, Class, Class)} method to create default scoped classes.
     * <p>
     * Method is thread safe.
     *
     * @param type interface or abstract class
     * @param <T>  type
     * @return implementation class for provided type (will not generate if class already exist)
     */
    public static <T> Class<T> generate(final Class<T> type) {
        return generate(type, null);
    }

    /**
     * Shortcut for {@link #generate(Class, Class, Class)} method to create classes with provided scope
     * (and without extra anchor).
     * <p>
     * Method is thread safe.
     *
     * @param type  interface or abstract class
     * @param scope scope annotation to apply on generated class (may be null for default prototype scope)
     * @param <T>   type
     * @return implementation class for provided type (will not generate if class already exist)
     */
    public static <T> Class<T> generate(final Class<T> type,
                                        final Class<? extends java.lang.annotation.Annotation> scope) {
        return generate(type, scope, null);
    }

    /**
     * Generates dynamic class, which guice may use as implementation and generate proxy above it,
     * correctly applying aop features.
     * <p>
     * New class will inherit type annotations and constructor with annotations
     * (if base class use constructor injection). Also constructor inherits all annotations, including
     * parameters annotations. If anchor is provided then it will be added as last constructor parameter
     * or (when abstract type has no constructor) new constructor added with one parameter (anchor).
     * <p>
     * Method is thread safe.
     *
     * @param type   interface or abstract class
     * @param scope  scope annotation to apply on generated class (may be null for default prototype scope)
     * @param anchor existing binding to depend generated class on (to prevent binding bubbling up to root injector)
     * @param <T>    type
     * @return implementation class for provided type (will not generate if class already exist)
     * @see ru.vyarus.guice.ext.core.generator.anchor.GeneratorAnchorModule for more details about anchor usage
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> generate(final Class<T> type,
                                        final Class<? extends java.lang.annotation.Annotation> scope,
                                        final Class<?> anchor) {
        Preconditions.checkNotNull(type, "Original type required");
        Preconditions.checkArgument(type.isInterface() || Modifier.isAbstract(type.getModifiers()),
                "Type must be interface or abstract class, but provided type is not: %s", type.getName());

        final String targetClassName = type.getName() + DYNAMIC_CLASS_POSTFIX;
        final ClassLoader classLoader = type.getClassLoader();

        /*
         * Synchronization is required to avoid double generation and consequent problems.
         * Very unlikely that this method would be called too often and synchronization become bottleneck.
         * Using original class as monitor to allow concurrent generation for different classes.
         */
        synchronized (type) {
            Class<?> targetClass;
            try {
                // will work if class was already generated
                targetClass = classLoader.loadClass(targetClassName);
            } catch (ClassNotFoundException ex) {
                targetClass = generateClass(type, targetClassName, classLoader, scope, anchor);
            }
            return (Class<T>) targetClass;
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<?> generateClass(final Class<?> type, final String targetClassName,
                                          final ClassLoader classLoader,
                                          final Class<? extends java.lang.annotation.Annotation> scope,
                                          final Class<?> anchor) {
        try {
            // have to use custom pool because original type classloader could be thrown away
            // and all cached CtClass objects would be stale
            final ClassPool classPool = new ClassPool();
            classPool.appendClassPath(new LoaderClassPath(classLoader));

            final CtClass impl = generateCtClass(classPool, targetClassName, type, scope, anchor);
            return impl.toClass(classLoader, null);
        } catch (Exception ex) {
            throw new DynamicClassException("Failed to generate class for " + type.getName(), ex);
        }
    }

    private static CtClass generateCtClass(final ClassPool classPool, final String targetClassName, final Class type,
                                           final Class<? extends java.lang.annotation.Annotation> scope,
                                           final Class<?> anchor)
            throws Exception {

        final CtClass ctType = classPool.get(type.getName());
        final CtClass ctAnchor = anchor == null ? null : classPool.getCtClass(anchor.getName());
        final CtClass impl;
        if (type.isInterface()) {
            impl = classPool.makeClass(targetClassName);
            impl.addInterface(ctType);
        } else {
            impl = classPool.makeClass(targetClassName, ctType);
            final Constructor diConstructor = findDIConstructor(type);
            if (diConstructor != null) {
                copyConstructor(impl, ctType, diConstructor, ctAnchor);
            }
        }
        if (anchor != null && impl.getConstructors().length == 0) {
            // create new constructor with anchor dependency
            createAnchorConstructor(impl, ctAnchor);
        }
        final ConstPool constPool = impl.getClassFile().getConstPool();
        final AnnotationsAttribute annotations = copyAnnotations(classPool, constPool, type);
        impl.getClassFile().addAttribute(annotations);
        applyScopeAnnotation(classPool, annotations, type, scope);
        return impl;
    }

    private static void copyConstructor(final CtClass impl, final CtClass ctType, final Constructor ctor,
                                        final CtClass anchor) throws Exception {
        final ClassPool classPool = impl.getClassPool();
        final CtClass[] parameters = JavassistUtils.convertTypes(classPool, ctor.getParameterTypes());
        final CtConstructor ctConstructor = CtNewConstructor.make(
                parameters,
                JavassistUtils.convertTypes(classPool, ctor.getExceptionTypes()),
                CtNewConstructor.PASS_PARAMS, null, null, impl);
        if (anchor != null) {
            ctConstructor.addParameter(anchor);
        }
        final ConstPool constPool = impl.getClassFile().getConstPool();
        final MethodInfo methodInfo = ctConstructor.getMethodInfo();
        methodInfo.addAttribute(copyAnnotations(classPool, constPool, ctor));
        methodInfo.addAttribute(copyConstructorParametersAnnotations(classPool, constPool, ctor, anchor != null));
        final SignatureAttribute info = copyConstructorGenericsSignature(constPool, parameters, ctType, anchor);
        if (info != null) {
            methodInfo.addAttribute(info);
        }
        impl.addConstructor(ctConstructor);
    }

    private static void createAnchorConstructor(final CtClass impl, final CtClass anchor)
            throws Exception {
        final ClassPool classPool = impl.getClassPool();
        final CtConstructor ctConstructor = CtNewConstructor.make(
                new CtClass[]{anchor},
                null,
                CtNewConstructor.PASS_NONE, null, null, impl);
        final ConstPool constPool = impl.getClassFile().getConstPool();
        final MethodInfo methodInfo = ctConstructor.getMethodInfo();
        // add injection annotation
        final AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        final Annotation annotation = new Annotation(constPool,
                classPool.get(javax.inject.Inject.class.getName()));
        attr.addAnnotation(annotation);
        methodInfo.addAttribute(attr);
        impl.addConstructor(ctConstructor);
    }

    private static Constructor findDIConstructor(final Class<?> type) {
        Constructor target = null;
        for (Constructor ctor : type.getConstructors()) {
            if (ctor.isAnnotationPresent(Inject.class) || ctor.isAnnotationPresent(javax.inject.Inject.class)) {
                target = ctor;
                break;
            }
        }
        return target;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static ParameterAnnotationsAttribute copyConstructorParametersAnnotations(
            final ClassPool classPool, final ConstPool constPool, final Constructor ctor,
            final boolean anchorAdded) throws Exception {
        final int count = ctor.getParameterTypes().length;
        final Annotation[][] paramAnnotations = new Annotation[count + (anchorAdded ? 1 : 0)][];
        for (int i = 0; i < count; i++) {
            final java.lang.annotation.Annotation[] anns = ctor.getParameterAnnotations()[i];
            paramAnnotations[i] = new Annotation[anns.length];
            for (int j = 0; j < anns.length; j++) {
                paramAnnotations[i][j] = JavassistUtils.copyAnnotation(classPool, constPool, anns[j]);
            }
        }
        if (anchorAdded) {
            paramAnnotations[count] = new Annotation[0];
        }
        final ParameterAnnotationsAttribute paramAnns = new ParameterAnnotationsAttribute(
                constPool, ParameterAnnotationsAttribute.visibleTag);
        paramAnns.setAnnotations(paramAnnotations);
        return paramAnns;
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static void applyScopeAnnotation(final ClassPool classPool, final AnnotationsAttribute annotations,
                                             final AnnotatedElement source,
                                             final Class<? extends java.lang.annotation.Annotation> scope)
            throws Exception {
        if (scope != null) {
            Preconditions.checkState(Annotations.isScopeAnnotation(scope),
                    "Provided annotation %s is not scope annotation", scope.getSimpleName());
            for (java.lang.annotation.Annotation ann : source.getAnnotations()) {
                Preconditions.checkArgument(!(ann instanceof ScopeAnnotation),
                        "Duplicate scope definition: scope is specified as %s and also defined "
                                + "in @ScopeAnnotation.", scope.getSimpleName());
            }
            annotations.addAnnotation(new Annotation(annotations.getConstPool(), classPool.get(scope.getName())));
        }
    }

    private static AnnotationsAttribute copyAnnotations(final ClassPool classPool, final ConstPool constPool,
                                                        final AnnotatedElement source) throws Exception {
        final AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        if (source.getAnnotations().length > 0) {

            for (java.lang.annotation.Annotation ann : source.getAnnotations()) {
                final Annotation annotation = processAnnotation(classPool, constPool, ann);
                if (annotation != null) {
                    attr.addAnnotation(annotation);
                }
            }
        }
        return attr;
    }

    private static Annotation processAnnotation(final ClassPool classPool, final ConstPool constPool,
                                                final java.lang.annotation.Annotation ann) throws Exception {
        Annotation res = null;
        // if we copy these annotation guice will go to infinite loop
        if (!(ann instanceof ProvidedBy || ann instanceof ImplementedBy)) {
            Preconditions.checkState(!Annotations.isScopeAnnotation(ann.annotationType()),
                    "Don't use scope annotations directly - use @ScopeAnnotation(TargetScope) wrapper, "
                            + "because guice doesn't allow scope annotations on abstract types");
            if (ann instanceof ScopeAnnotation) {
                res = new Annotation(constPool,
                        classPool.get(((ScopeAnnotation) ann).value().getName()));
            } else {
                res = JavassistUtils.copyAnnotation(classPool, constPool, ann);
            }
        }
        return res;
    }

    private static SignatureAttribute copyConstructorGenericsSignature(
            final ConstPool constPool, final CtClass[] params, final CtClass source, final CtClass anchor)
            throws Exception {
        final CtConstructor ctConstructor = source.getConstructor(Descriptor.ofConstructor(params));
        String signature = null;
        for (Object attr : ctConstructor.getMethodInfo().getAttributes()) {
            if (attr instanceof SignatureAttribute) {
                signature = ((SignatureAttribute) attr).getSignature();
                break;
            }
        }
        if (signature != null && anchor != null) {
            // add anchor to generics signature
            final String type = "L" + (anchor.getName().replaceAll("\\.", "/")) + ";";
            final int idx = signature.lastIndexOf(')');
            signature = signature.substring(0, idx) + type + signature.substring(idx);
        }
        return signature == null ? null : new SignatureAttribute(constPool, signature);
    }
}
