#Additinoal guice annotations
[![Build Status](https://travis-ci.org/xvik/guice-ext-annotations.svg?branch=master)](https://travis-ci.org/xvik/guice-ext-annotations)
[ ![Download](https://api.bintray.com/packages/vyarus/xvik/guice-ext-annotations/images/download.png) ](https://bintray.com/vyarus/xvik/guice-ext-annotations/_latestVersion)

### About

Supported annotations:
* @Log: auto inject slf4j logger
* JSR-250 @PostConstruct: annotated method called after bean initialization
* JSR-250 @PreDestroy: annotated method called before shutdown

Also provides utilities to easily add new annotations support.

### Setup

Releases are published to [bintray jcenter](https://bintray.com/bintray/jcenter) (package appear immediately after release) 
and then to maven central (require few days after release to be published). 

Maven:

```xml
<dependency>
  <groupId>ru.vyarus</groupId>
  <artifactId>guice-ext-annotations</artifactId>
  <version>1.0.0</version>
</dependency>
```

Gradle:

```groovy
compile 'ru.vyarus:guice-ext-annotations:1.0.0'
```

### Install the Guice module

```java
install(new ExtAnnotationsModule());
```

To limit processed beans to specific package use:

```java
install(new ExtAnnotationsModule("your.package"));
```

Alternatively custom object matcher may be used to reduce processed beans:

```java
install(new ExtAnnotationsModule(new YourCustomMatcher()));
```

### Usage

##### @Log

Annotate logger filed:

```java
@Log
private org.slf4j.Logger logger
```

Only Slf4j logger supported. Trying to use annotation with other logger will throw exception on initialization.

##### @PostConstruct

Annotate bean method to be called after bean initialization.

```java
@PostConstruct
private void init() { 
    // init logic 
}
```

Annotated method must not contain parameters or exception will be thrown on initialization.

##### @PreDestroy

Annotate bean method to be called before shutdown (by default before jvm shutdown).

```java
@PreDestroy
private void destroy() { 
    // destroy logic 
}
```

Annotated method must not contain parameters or exception will be thrown on initialization.

Destroy processing may be triggered manually by:

```java
injector.getBean(ru.vyarus.guice.ext.managed.destroyable.DestroyableManager.class).destroy()
```
Useful if guice used in conjunction with some other container (e.g. web container). Destroy may be called any number of times, 
but actual destroy will be processed only on first execution.

##### Destroyable

As an alternative to `@PreDestroy`, bean may implement `ru.vyarus.guice.ext.managed.destroyable.Destroyable`

```java
public class MyBean implements Destroyable
```

### Additional api

##### Custom field annotation post processor

Implement `ru.vyarus.guice.ext.core.field.FieldPostProcessor`. 
Handle only recoverable exceptions, otherwise it will be handled by `AnnotatedFieldTypeListener`.

For example:

```java
public class Slf4jLogAnnotationProcessor implements FieldPostProcessor<Log> {

    @Override
    public void process(final Log annotation, final Field field, final Object instance) throws Exception {
        final Logger logger = LoggerFactory.getLogger(field.getDeclaringClass());
        field.set(instance, logger);
    }
}
```

Register in module:

```java
bindListener(typeMatcher, new AnnotatedFieldTypeListener<Log>(
                Log.class, new Slf4jLogAnnotationProcessor()));
```

##### Custom method annotation post processor

Implement `ru.vyarus.guice.ext.core.method.MethodPostProcessor`. 
Handle only recoverable exceptions, otherwise it will be handled by `AnnotatedMethodTypeListener`.

For example:

```java
public class PostConstructAnnotationProcessor implements MethodPostProcessor<PostConstruct> {

    @Override
    public void process(PostConstruct annotation, Method method, Object instance) throws Exception {
        Utils.checkNoParams(method);
        method.invoke(instance);
    }
}
```

Register in module:

```java
bindListener(typeMatcher, new AnnotatedMethodTypeListener<PostConstruct>(
                PostConstruct.class, new PostConstructAnnotationProcessor()));
```

##### Custom type post processor

Implement `ru.vyarus.guice.ext.core.type.TypePostProcessor`.
Handle only recoverable exceptions, otherwise it will be handled by `GeneralTypeListener`.

For example:

```java
public class DestroyableTypeProcessor implements TypePostProcessor<Destroyable> {
    private DestroyableManager manager;

    public DestroyableTypeProcessor(DestroyableManager manager) {
        this.manager = manager;
    }

    @Override
    public void process(Destroyable instance) {
        manager.register(instance);
    }
}
```

Register in module:

```java
bindListener(typeMatcher, new GeneralTypeListener<Destroyable>(
                Destroyable.class, new DestroyableTypeProcessor(manager)));
```

`GeneralTypeListener` match bean by exact type, super type or interface (direct or super type interface).

### Thanks to

[99soft lifegycle](https://github.com/99soft/lifegycle) for inspiration.

### Licence

MIT
