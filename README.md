#Guice annotations extensions
[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/xvik/guice-ext-annotations?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](http://www.opensource.org/licenses/MIT)
[![Build Status](http://img.shields.io/travis/xvik/guice-ext-annotations.svg?style=flat&branch=master)](https://travis-ci.org/xvik/guice-ext-annotations)
[![Coverage Status](https://img.shields.io/coveralls/xvik/guice-ext-annotations.svg?style=flat)](https://coveralls.io/r/xvik/guice-ext-annotations?branch=master)

### About

Guice annotations support extensions.

Features:
* Allows using interfaces and abstract classes as guice beans: abstract methods must be handled with guice aop
* Additional annotations:
  * @Log: auto inject slf4j logger
  * JSR-250 @PostConstruct: annotated method called after bean initialization
  * JSR-250 @PreDestroy: annotated method called before shutdown
* Simplifies [TypeListener](http://google.github.io/guice/api-docs/latest/javadoc/index.html?com/google/inject/spi/TypeListener.html) api for cases:
  * Add custom field annotation support
  * Add custom method annotation support
  * Add post processing for beans of some type (e.g. implementing interface or extending some abstract class)

### Setup

Releases are published to [bintray jcenter](https://bintray.com/bintray/jcenter) (package appear immediately after release) 
and then to maven central (require few days after release to be published). 

[![Download](https://api.bintray.com/packages/vyarus/xvik/guice-ext-annotations/images/download.svg) ](https://bintray.com/vyarus/xvik/guice-ext-annotations/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/ru.vyarus/guice-ext-annotations/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus/guice-ext-annotations)

Maven:

```xml
<dependency>
  <groupId>ru.vyarus</groupId>
  <artifactId>guice-ext-annotations</artifactId>
  <version>1.1.0</version>
</dependency>
```

Gradle:

```groovy
compile 'ru.vyarus:guice-ext-annotations:1.1.0'
```

### Abstract types support

Feature was developed to support finders in [guice-persist-orient](https://github.com/xvik/guice-persist-orient#dynamic-finders-1)
(look usage examples).

#### Problem

Suppose you have annotation to simplify sql query definition:

```java
@Finder(query = "select from Model")
List<Model> list() {
    throw new UnsupportedOperationException();
}
```

Support for such annotation could be easily implemented with [guice aop](https://github.com/google/guice/wiki/AOP).
But we will always have to declare method body.

To avoid this, [guice-persist](https://github.com/google/guice/wiki/GuicePersist) creates JDK proxies from interfaces with annotated methods.
It solves problem with redundant method body, but did not allow using guice aop on such beans.

#### Solution

Guice needs to control bean instance creation to properly apply aop, so solution is simple: dynamically create
implementation class from abstract class or interface, so guice could instantiate bean from it.

Additional actions during class generation:
* Annotations copied from abstract type (class or interface) to allow aop correctly resole them
* If abstract bean use constructor injection, the same constructor will be created in implementation (including all
constructor and parameters annotations).

All this allows thinking of abstract type as of usual guice bean.

#### Setup

In order to use dynamic proxies, add dependency on javassist library:

```groovy
optional 'org.javassist:javassist:3.16.1-GA'
```

NOTE: javassist used instead of cglib, because cglib can't manipulate annotations.

#### Usage

There are two options: declare bean directly in module or rely on JIT based declaration.

For first option, generate class in your guice module:

```java
bind(MyAbstractType.class).to(DynamicClassGenerator.generate(MyAbstractType.class));
```

If you don't want to declare manually and prefer minimal configuration with
[JIT](https://github.com/google/guice/wiki/JustInTimeBindings) bindings, use `@ProvidedBy`:

```java
@ProvidedBy(DynamicClassProvider.class)
public interface MyAbstractType {...}
```

(the same for abstract class)

After all, bean could be injected as usual:

```java
@Inject MyAbstractType myBean;
```

Don't forget that all abstract methods must be handled with aop: otherwise you will get abstract method call exception.

#### Limitation

There is only one limitation: you can't use scope annotations directly on abstract types - guice doesn't allow it.
To workaround it use wrapper annotation:

```java
@ScopeAnnotation(Singleton.class)
@ProvidedBy(DynamicClassProvider.class)
public interface MyAbstractType {...}
```

NOTE: yes, annotation is named the same as guice's own annotation, but name is so good and they will never met in one class.

### Additional annotations

Guice module adds three annotations support (`@Log`, `@PostConstruct`, `@PreDestroy`) and `Destroyable` types.

#### Install the Guice module

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

#### Usage

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

Api simplifies work with [TypeListener](http://google.github.io/guice/api-docs/latest/javadoc/index.html?com/google/inject/spi/TypeListener.html).

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
    public void process(final PostConstruct annotation, final Method method, final Object instance) throws Exception {
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

    public DestroyableTypeProcessor(final DestroyableManager manager) {
        this.manager = manager;
    }

    @Override
    public void process(final Destroyable instance) {
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

-
[![Slush java lib generator](http://img.shields.io/badge/Powered%20by-Slush%20java%20lib%20generator-orange.svg?style=flat-square)](https://github.com/xvik/slush-lib-java)
