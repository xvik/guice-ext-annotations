package ru.vyarus.guice.ext.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guice.ext.core.field.FieldPostProcessor;

import java.lang.reflect.Field;

/**
 * Injects {@code org.slf4j.Logger} instance into fields annotated with @Log annotation.
 *
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public class Slf4jLogAnnotationProcessor implements FieldPostProcessor<Log> {

    @Override
    public void process(final Log annotation, final Field field, final Object instance) throws Exception {
        final Logger logger = LoggerFactory.getLogger(field.getDeclaringClass());
        field.set(instance, logger);
    }
}
