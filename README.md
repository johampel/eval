# Library for evaluating math expressions

This library is intended to evaluate math expressions for complex and real numbers.

## Quick start

Before starting you have to add this library as a dependency to your project, for example
for maven based projects, add the following to the `dependencies` section:
```xml
    <dependency>
      <groupId>de.hipphampel.eval</groupId>
      <artifactId>eval</artifactId>
      <version> -- Place current version here -- </version>
    </dependency>
```
Having that in place, you should be able to write something like this, to evaluate
a simple expression:
```java
    DoubleContext context = DoubleContext.standard();
    double result = context.evaluate("(2+3)*5");
    System.err.println(result);                        // prints "25.0"
```
So all you have to do is to create a `DoubleContext` and call the `evaluate` method on it. In a
context created this way there are also some commonly used constants and functions, so it is
also perfect to write:
```java
    result = context.evaluate("sin(pi/2)");   // Calculate sine of pi/2, or sine of 90 degrees
    System.err.println(result);               // prints "1.0"
```
A detailed list of the predefined constants and functions can be found in the following sections.
You may like define your own constants or functions as well:
```java
    context.constant("two", 2.0)
           .function("f", List.of("x", "y"), "x^y + y^x"); // f(x,y) = x^y + y^x
    result = context.evaluate("f(two, 3)");
    System.err.println(result);                            // prints "17.0"
```
It is also possible to define variables; in opposite to constants the value of a variable can be
changed:
```java
    context.variable("x", 1.0);
    result = context.evaluate("f(x, two)");
    System.err.println(result);              // prints "3.0"

    context.variable("x", 2.0);
    result = context.evaluate("f(x, two)");
    System.err.println(result);              // prints "8.0"
```
All in all a context acts as an object that is able to evaluate expressions and contains definitions
for functions, variables, and constants.

## Other types than `double`

In the quick start we learnt how to do evaluations for the data type `double`, which is quite handy
since this type is a primitive; but sometime this is not sufficient: maybe you want to make your
computations based on complex numbers or with a higher precision than `double` supports.

For this there is also a support for other types than `double`, one example is the support for
complex numbers:

Complex numbers are represented by `Apcomplex` from the [apfloat project](http://www.apfloat.org/)
to represent number. So, if you want to deal with complex numbers, use the `ApcomplexContext`
instead of a `DoubleContext`,
like this:
```java
    ApcomplexContext context = ApcomplexContext.standard();
    Apcomplex result = context.evaluate("(-1)^0.5");
    System.err.println(result);                        // prints "(0.0, 1.0)", means 0.0+1.0i
```
Note that there are also overloaded version of `ApcomplexContext.standard`
or `ApcomplexContext.minimal`
to specify an alternative precision instead of the default one with 100.

All in all, the following types are supported:

| Number type  | Context to use      | Remarks                                                    |
|--------------|---------------------|------------------------------------------------------------|
| `double`     | `DoubleContext`     | Real numbers, fixed precision of 16 digits                 |
| `Apcomplex`  | `ApcomplexContext`  | Complex numbers, default precision 34 digits, configurable |
| `BigDecimal` | `BigDecimalContext` | Real numbers, default precision 34 digits, configurable    |     
| `Apfloat`    | `ApfloatContext`    | Real numbers, default precision 34< digits, configurable   |

It is also easy to roll your own implementation, by extending the class `Context` which acts as a
base class for all other implementations.

# Functions and constants

## Predefined functions and constants

No matter, which number type you choose, if you create your context using the `standard()` factory
method, such as `DoubleContext.standard()` or `ApcomplexContext.standard()`, a set of comonly used
constants and functions is already predefined.

For all contexts, the constants `pi` (=`3.14...`) and the Euler number `e` (= `2.71...`) is present,
especially for the `ApcomlexContext` also the imaginary unit `i`.

For all the context implementations the `standard()` factory method also defines the following
functions:

- Trignometric functions: `sin`, `cos`, `tan`
- The inverse of them: `asin`, `acos`, `atan`
- Hyperbolic functions: `sinh`, `cosh`, `tanh`
- The inverse of them: `asinh`, `acosh`, `atanh`
- Logarithms: `ln` (natural logarithm), `log` (logarithm for any base)

For the `ApcomplexContext` in addition we have:

- `real` and `imag`, `abs`and `arg`
- `norm`.

## User defined functions

If you what to define your own functions, you have basically two choices:

The most simple way is to define it via a parseable expression. For example, in case you want to
define the geometric mean of two values `x` and `y`, you may define a function like this:
```java
    //               function name, function parameters, definition
    context.function("geomean",     List.of("x", "y"),   (x*y)^0.5);
```
After this you may write:
```java
    context.evaluate("geomean(4, 3)"); // Something like 3.46...
```
If this is not sufficient, you may decide to implement the interface `FunctionDefinition` on your
own. The following defines also `geomean` but now accepting as many parameters you like:
```java
    public class Geomean implements FunctionDefinition {
        @Override
        public String name() { return "geomean"; }
    
        @Override
        public int minArgs() { return 1; }
    
        @Override
        public int maxArgs() { return Integer.MAX_VALUE; }
    
        @Override
        public Apcomplex evaluate(Context<?, ?> context, List<Apcomplex> args) {
            FixedPrecisionApcomplexHelper helper = context.precisionHelper();
            Apcomplex product = args.stream().reduce(Apcomplex.ONE, helper::multiply);
            Apcomplex exp = helper.divide(Apcomplex.ONE, new Apcomplex(new Apfloat(args.size()))); 
            return context.precisionHelper().pow(product, exp);
        }
    }
```
After this you may write:
```java
    context.function(new Geomean());
    context.evaluate("geomean(4, 6, 7, 2, 3)"); 
```
Note that when implementing `FunctionDefinition` you always have to compute with `Apcomplex`.

# Parsing

## Parse mode `STANDARD`

By default (mode `STANDARD`), expressions are parsed using a parser that has quite strict rules; due
to their
strictness
they are easy to understand, but - on the other hand - in certain situations a little bit
cumbersome.

In the default processing mode, the parser knows the following structures:

| Type                    | Description                                                                                                                                     | Examples                                |
|-------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------|
| Values                  | Number literals. In general, any string representation that is common on Java for double values will work                                       | `1`, `1.2`, `.2`, `1.`, `1e3`, `1.2e-4` |
| Value names             | Names of variables or values. Generally, such names must consist of aplhabetic characters only                                                  | `e`, `pi`, `foo`, `x`                   |
| Grouping                | Grouping of termss via simple parenthesis                                                                                                       | `(x+y)`                                 |
| Function calls          | A call to a function                                                                                                                            | `sin(x)`, `f(x+y7, 2)`                  |
| Unary sign              | A sign                                                                                                                                          | `+1`, `-x`, '+f(x)`                     |
| Power of                | Binary operation for power of. The "power of" operator is the `^`sign. The operator is right associative, so `2^3^2` is the same like `2^(3^2)` | `e^x`, `2^3^4`                          |
| Division/multiplication | Binary operation for multiplication (`*`) and division (`/`). These operators are left associative, so  `2*3*2` is the same like `(2*3)*2`      | `4*x`, `x/y`                            | 
| Addition/substraction   | Binary operation for addition (`+`) and substraction (`-`). These operators are left associative, so  `2-4+2` is the same like `(2-4)+2`        | `4+x`, `x-y`                            | 

All these structures have have the precedence listed above, so power of is evaluated before
multiplication and multiplication before addition, so that terms like `a+b*c^d*e` are evaluated in
their natural order, in this case like `a+((b*(c^d))*e)`.

Especially two aspects might be a little bit surprising and/or cumbersome:

- Surprising might be the fact that `-2^2` evaulates to `4`, since to the rules described above,
  `-2^2` is effectively evaluated as `(-2)^2`.
- It is cumbersome, because in case you want to express power of with a product as exponent has to
  be written like `e^(2*pi*x)`, more natural would be the notation `e^2pix`.

## Parse mode `SIMPLIFIED`

The simplified parse mode can be activated by calling the `parseMode` method of the context, like:
```java
    ApcomplexContext context = ApcomplexContext.standard()
        .parseMode(ParseMode.SIMPLIFIED);
```
This simplified mode has some similarities with the standard mode - in fact all expressions
parseable
in the standard mode can be parse in mode simplified as well - but there are also some noteable
differences.

The main difference is the concept of _convenience mulitplication_: Instead of explicitly writting
the `*` sign to express a multiplication, you are allowed to write `4x` incase you mean `4*x`, or
`(x+1)(x-1)`, if you actually mean `(x+1)*(x-1)`. This has several implications:

- Having a context knowing the variables or constants `a`,  `b`, `c` and `ab`, then the term `ab`
  will evaluate to `ab`, but `ac` to  `a*c`. This is because the names of variables are always
  matched
  greedy, so the longest variable name wins (If you want to express `a*b` without usage of the `*`
  you
  could write `a b`, but this is not recommended).
- To avoid ambiguities, it is not allowed to use the exponential notation for numbers, so the term
  `1e2` is seen as `1*e*2` not as a alternative value notation for `100`.
- The precedence rules of the convenience multiplication and the power of operator are defined
  as follows: if the base of the power of operation is an convenience multiplication, the power of
  is evaulated first; on the other hand: if the exponent is a convenience multiplication is
  evaluated
  first. A concrete example, where `x` is a variable: `4x^3x` is written as an expression parseable
  for the standard mode `4*x^(3*x)`, or more verbose `4*(x^(3*x))`
- A second precedence rule that differs from the standard is the unary minus, which has a lower
  precedence as the power of, so that `-2^2` is actually `-4`, because it is parsed as `-(2^2)`.

All other rules are the same like in mode standard.

  
