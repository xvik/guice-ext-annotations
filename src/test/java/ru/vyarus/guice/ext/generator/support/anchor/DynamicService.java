package ru.vyarus.guice.ext.generator.support.anchor;

import com.google.inject.Inject;

/**
 * @author Vyacheslav Rusakov
 * @since 22.09.2016
 */
public class DynamicService {
    @Inject
    TestIface service;

    public String hello() {
        return service.hello();
    }
}
