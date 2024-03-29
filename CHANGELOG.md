### 2.0.1 (2023-04-13)
* Avoid direct javax.inject annotations usage for jakarta.inject compatibility
  (guice native annotations used instead)
  - Dynamic class generation would support both annotations

### 2.0.0 (2023-04-02)
* Switch to jakarta.annotation-api 2 (jakarta. namespace)

NOTE: guice still use javax.inject

### 1.4.1 (2023-04-02)
* Replace javax.annotation-api jar with jakarta.annotation-api 1 (preserving javax. namespace)

### 1.4.0 (2021-09-06)
* Drop java 1.6 and 1.7 support
* Update to guice 5.0.1
* Fix JDK 16 compatibility for class generator (min required javassist is 3.24)

### 1.3.0 (2018-04-01)
* Guice 4.2.0 compatibility 

### 1.2.1 (2016-09-23)
* Fix DynamicClassProvider and DynamicSingletonProvider providers to be singletons.

### 1.2.0 (2016-09-23)
* Fix class generation for dynamic class loaders cases (required, for example, for playframework dev mode):
    - dynamic classes are checked now against class loader of original type
    - different classes will be generated for the same type from different class loaders
* Make class generator thread safe (fixes concurrent provider calls issue) 
* Update to guice 4.1
* Add ability to use dynamic providers in child injector or inside private module: GeneratorAnchorModule (bubble up fix)

### 1.1.1 (2015-01-06)
* Fix recognition of javax.inject.Inject annotation during class generation
* Generated class now contains valid generics signature (required for Provided parameters)
* Add DynamicSingletonProvider: shortcut for DynamicClassProvider to produce singleton bean (without need for additional annotation)
* Fix type post processor abstract class recognition

### 1.1.0 (2014-12-14)
* Update guice 3.0 -> 4.0-beta5
* Add DynamicClassGenerator: interfaces and abstract classes may be used as guice beans with complete aop support
* Add DynamicClassProvider for abstract classes and interfaces to use in @ProvidedBy (JIT resolution will trigger dynamic class generation )

### 1.0.1 (2014-08-16)
* Fix pmd/checkstyle warnings

### 1.0.0 (2014-07-02)
* Initial release