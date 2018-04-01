# Guice annotations extensions
[![License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat)](http://www.opensource.org/licenses/MIT)
[![Build Status](http://img.shields.io/travis/xvik/guice-ext-annotations.svg?style=flat&branch=master)](https://travis-ci.org/xvik/guice-ext-annotations)
[![Appveyor build status](https://ci.appveyor.com/api/projects/status/github/xvik/guice-ext-annotations?svg=true)](https://ci.appveyor.com/project/xvik/guice-ext-annotations)
[![codecov](https://codecov.io/gh/xvik/guice-ext-annotations/branch/master/graph/badge.svg)](https://codecov.io/gh/xvik/guice-ext-annotations)

Support: [Gitter chat](https://gitter.im/xvik/guice-ext-annotations)

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

### Thanks to

* [Derric Gilling](https://github.com/dgilling) for help with playframework related issues

### Setup

Releases are published to [bintray jcenter](https://bintray.com/bintray/jcenter) (package appear immediately after release) 
and then to maven central (require few days after release to be published). 

[![JCenter](https://api.bintray.com/packages/vyarus/xvik/guice-ext-annotations/images/download.svg)](https://bintray.com/vyarus/xvik/guice-ext-annotations/_latestVersion)
[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus/guice-ext-annotations.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus/guice-ext-annotations)

Maven:

```xml
<dependency>
  <groupId>ru.vyarus</groupId>
  <artifactId>guice-ext-annotations</artifactId>
  <version>1.2.1</version>
</dependency>
```

Gradle:

```groovy
compile 'ru.vyarus:guice-ext-annotations:1.2.1'
```

### Abstract types support

Feature was developed to support repositories in [guice-persist-orient](https://github.com/xvik/guice-persist-orient#defining-repository)
(look usage examples).

#### Problem

Suppose you have annotation to simplify sql query definition:

```java
@Query("select from Model")
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
implementation class from abstract class or interface, and let guice instantiate bean from it.

Additional actions during class generation:
* Annotations copied from abstract type (class or interface) to allow aop correctly resolve them
* If abstract bean use constructor injection, the same constructor will be created in implementation (including all
constructor and parameters annotations and generic signatures).

All this allows thinking of abstract type as of usual guice bean.

#### Setup

In order to use dynamic proxies, add dependency on javassist library:

```groovy
compile 'org.javassist:javassist:3.16.1-GA'
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

#### Class loaders

May be used within complex classloader hierarchies (like playframework dev mode). 

Class loader of abstract type is used to generate implementation class. Generated class is first checked fo existence in this 
class loader and, if not found, generated and attached to that class loader. For example, if the same class will be loaded by different
class loaders, then generator will generate different implementations.
In play dev mode this will mean that after hot reload (classloader with your abstract class replace) generator will 
generate new implementation for new (updated) abstract class.

Class generation is thread safe: synchronized on abstract type to allow concurrent generation for different classes and prevent
duplicate generations.

#### Limitation

There is only one limitation: you can't use scope annotations directly on abstract types - guice doesn't allow it.
To workaround it use wrapper annotation:

```java
@ScopeAnnotation(Singleton.class)
@ProvidedBy(DynamicClassProvider.class)
public interface MyAbstractType {...}
```

NOTE: yes, annotation named the same as guice's own annotation, but name is so good and they will never met in one class.

#### Singletons

Singletons are pretty common case. To simplify singletons definition special provider could be used:

```java
@ProvidedBy(DynamicSingletonProvider.class)
public interface MyAbstractType {...}
```

This is completely equivalent to code in limitations section, but requires just one annotation.

If, by accident, singleton provider will be used with `@ScopeAnnotation`, error will be thrown.

#### Child injectors and private modules

When JIT (dynamic) resolution is used (bindings not described in module) then, during dynamic binding creation,
guice will try to create binding in upper most injector (in order to re-use instance in possibly other
child injectors (java class loaders works the same way by the same reason)).

For example, suppose there is some root injector and your module with aop (used to handle annotations on abstract classes)
is declared in child injector.

```java
Guice.createInjector().createChildInjector(new MyAopModule());
```

Some abstract type without explicit binding:

```java
@ProvidedBy(DynamicClassProvider.class)
public interface MyAbstractBean {}
```

If some service depends on this abstract type (injects it), then JIT binding for MyAbstractBean will be created
in root(!) module. But aop to handle your custom annotations is declared in child module and so you will get
abstract method call exception when try to call any method of abstract bean.

For example, such case could appear with test-ng guice integration, which always start your modules as child injector.

In general, there are two ways to workaround such situation:
* Manually declare all required bindings in child module
* Abstract type must depend on some other bean in child injector (this will force JIT binding to be created in child module (prevent bubbling up))

##### Solution 

Out of the box, special module is available to solve this problem: `GeneratorAnchorModule`.

If it will be used in example above, then JIT bindings for abstract types will be created in child injector automatically (fixing behaviour):

```java
Guice.createInjector().createChildInjector(new MyAopModule(), new GeneratorAnchorModule());
```

Module use "anchor" technic: dummy bean (AnchorBean) is registered in child injector and all dynamically generated classes
(when `@ProvidedBy` used) become dependent of this dummy bean (AnchorBean added to generated implementation class constructor). 

Also, module will allow using dynamic bindings inside private module:

```java
    public class MyPrivateModule extends PrivateModule {
        protected void configure() {
            install(new MyAopModule());
            install(new GeneratorAnchorModule());
        }

        @Provides 
        @Exposed 
        @Named("myBean")
        MyAbstractBean provide(MyAbstractBean bean) {
            return bean;
        }
    }

     Injector injector = Injector.createModule(new MyPrivateModule());
     injector.getInstance(Key.get(MyAbstractBean.class, Names.named("myBean")))
```

Note that MyAbstractBean is not bound explicitly, but still correct instance exposed from private module.

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

Example of [post processing beans extending abstract class](https://github.com/xvik/guice-ext-annotations/blob/master/src/test/java/ru/vyarus/guice/ext/postprocess/TypePostProcessorTest.java)

Listener binding may seem to be more complicated than it could: typeMatcher could filter types instead of custom logic.
This was done on purpose - to allow using mather for appliance scoping. For example, if only beans from exact packages
should be processed, or any other conditions.

### Thanks to

[99soft lifegycle](https://github.com/99soft/lifegycle) for inspiration.

---
[![java lib generator](http://img.shields.io/badge/Powered%20by-%20Java%20lib%20generator-green.svg?style=flat-square)](https://github.com/xvik/generator-lib-java)
