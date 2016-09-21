package ru.vyarus.guice.ext.generator.support.anchor;

/**
 * Service must be resolved with JIT and so be created at parent injector
 * (so anchor addition is still required to move generated bean to child injector)
 *
 * @author Vyacheslav Rusakov
 * @since 21.09.2016
 */
public class RootService {
}
