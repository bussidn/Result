# Result
Railway oriented programming ?

## Motivation

Result is intended to be an alternative to checked and runtime exception and also to current Try monads (vavr, cyclops)

Checked exceptions are difficult to use in the lambda world.
Runtime exceptions are not explicit and requires attention instead of relying on the compiler
Try has the benefit over runtime to make it clear to the coder that a failure may occur but the type is not declared

Result is an alternative to these that make it clear and sound that a method can fail and what type of failure may occur.

The main drawback is that a bit of work will be necessary at failure typing level
This library intend to provide most of composition-generic-methods to easily compose your failure methods in the most common situations