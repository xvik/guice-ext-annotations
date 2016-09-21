package ru.vyarus.guice.ext.core.generator.anchor;

/**
 * Dummy bean used as anchor dependency for generated classes bindings (JIT bindings) to force them
 * to be registered in (for example) child injector.
 * <p>
 * For abstract classes annotated with {@code @ProvidedBy(DynamicClassProvider)} (or singleton variant),
 * provider will check if {@link AnchorBean} is available in injector and add extra dependency for it in
 * generated class constructor.
 * <p>
 * Used by {@link GeneratorAnchorModule}.
 *
 * @author Vyacheslav Rusakov
 * @see GeneratorAnchorModule for details
 * @since 21.09.2016
 */
public final class AnchorBean {
}
