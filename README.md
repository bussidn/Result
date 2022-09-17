# Result

Railway oriented programming ?

## Motivation

Result is intended to be an alternative to checked and runtime exception and also to current Try monads (vavr, cyclops)

Checked exceptions are difficult to use in the lambda world.
Runtime exceptions are not explicit and requires attention instead of relying on the compiler
Try has the benefit over runtime to make it clear to the coder that a failure may occur but the type is not declared

Result is an alternative to these that make it clear and sound that a method can fail and what type of failure may
occur.

The main drawback is that a bit of work will be necessary at failure typing level
This library intend to provide most of composition-generic-methods to easily compose your failure methods in the most
common situations

## How to get it


Add the jitpack repository to your repositories

You can find how to add this library to your project here :  
[![](https://jitpack.io/v/bussidn/Result.svg)](https://jitpack.io/#bussidn/Result)

For example with maven :
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Then add the dependency :

```xml
<dependency>
    <groupId>com.github.bussidn</groupId>
    <artifactId>Result</artifactId>
    <version>0.0.4</version>
</dependency>
```

## Quick Roadmap

### Done

- Result as a Success | Failure union type
- VoidResult
- Map functions (Function, Supplier)
- Function Level (ResultFunction / VoidResultFunction)
    - conversion methods
    - map methods
- bind methods
    - on Result (Function, Supplier)
    - on VoidResult (Supplier)
    - on ResultFunction (Function, Supplier)
    - on VoidResultFunction (Supplier)
- bind & bridge functions
    - on Result flatMapToVoid (Function, Supplier)
    - on VoidResult flatMapToResult
    - on ResultFunction flatMapToVoid (Function, Supplier)
    - on VoidResultFunction flatMapToResult
- recover
    - Result (Function & Supplier)
    - ResultFunction (Function & Supplier)
- tryRecovering
    - Result (Function & Supplier)
    - ResultFunction (Function & Supplier)
    - VoidResult (Function & Supplier)
    - VoidResultFunction (Function & Supplier)
- collect function (Results with reducing strategy)
- mapFailure (([Function, Supplier, Consumer] <*> [Result, ResultFunction, VoidResult, VoidResultFunction])
- structure helpers (from boolean & predicate)

### ToDo

#### V.1 ?

- structure helpers (from throwing function)
- aggregation methods
- transformation methods (Stream, Optional)
- bridge functions (toVoidResult, toVoidResultFunction, toVoid ?)

#### long term goals

- primitive results (intResult, doubleResult, etc.) ?
- Observable with failure type ?