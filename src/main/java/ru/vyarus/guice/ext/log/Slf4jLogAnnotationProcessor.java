package ru.vyarus.guice.ext.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.guice.ext.core.field.FieldPostProcessor;

import java.lang.reflect.Field;

/**
 * @author Vyacheslav Rusakov
 * @since 30.06.2014
 */
public class Slf4jLogAnnotationProcessor implements FieldPostProcessor<Log> {

    @Override
    public void process(Log annotation, Field field, Object instance) throws Exception {
        Logger logger = LoggerFactory.getLogger(field.getDeclaringClass());
        field.set(instance, logger);
    }
}
