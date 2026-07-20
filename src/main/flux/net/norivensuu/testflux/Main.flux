import java.lang.* // Inherits Java libraries and importing structure
using org.apfloat.Apfloat // Or you can use C# format if you want

float float1 = 1.0F // Java handles as float
float float2 = 1.0 // Same here

double double1 = 1.0 // Java handles as double
double double2 = 1.0d // Same here

String str1 = '\'string1\'' // Directly uses Java String type, boolean also works
string str2 = "\"string2\"" // The same type as before, just more consistent with built-ins

char char1 = 's' // Single symbol, technical or '' strings are treated as chars

bool bool1;

j = 0l;

var var1 = 1.0F // Handles value variables
var2 = 1.0F // Var is optional

var3 = varFunction1() // Also handles function variables

var4 = "hai"

public bool SampleFunction(float a, float b) { // Inherits modifiers from Java

    float c = float1 + float2; float d = 3.14 // Both ';' and '\n' serve as terminators

    float m1 = c ** d // Exponent operator
    float m2 = c /% d // Floor Division operator (aka // in python; /% points DOWN, meaning it's floor)
    float m3 = c %/ d // Ceil Division operator (%/ points UP, meaning it's ceil)

    m2 = c floor d // Can also be written verbosely
    m3 = c ceil d // Also variable declaration is automatically lowered for these entries because they were already defined

    m1 **= 2
    m2 /%= 2 // These three work the same way as any regular operation assignments
    m3 %/= 2

    modint1 = 5 % 2 // Division with remainder
    modint2 = 5 mod 2 // Also allows verboseness

    List<List<string>> list1 = new ArrayList() {{ add(List.of("element1")); add(List.of("element2")); }} // Directly compiles in Java
    list2 = [1, 2, 3, 4, 5] // Borrows simpler list structure from python

    bool1 = c < d < 5 // Chained comparisons are supported

    foreach (var element in list1) { // Foreach works both in C# format
        print(element, 'C# foreach') // Custom System.out.println wrapper
    }

    for (element : list1) { // And Java format (':' and 'in' are interchangeable for both)
        print(element, 'Java foreach')
    }

    bool ternaryBool = False if list1.size() > list2.size() else True // Python ternary is supported

    return 1 < 2 < 5 ? ternaryBool : False // The more classic alternative too
}

def main() { // Optional python-style block and function syntax (def is the same as void)
    print(double1 *** double2); // Tetration

    var test = SampleFunction(float1, float2)

    def voidDef(float voidFloat1, voidFloat2: float) { // You can combine python and java/C# syntax
        if (voidFloat1 ~~.5 voidFloat2) { // Approximation operator, will return yes if two values are approximately equal to (.precision) digits after the decimal point
            print("yay1")
        }
        if (voidFloat1 5.~~ voidFloat2) { // Integer notation before the . signals higher order approximation (52 1.~~ 56 is true because 50 == 50)
            print("yay2")
        }
        if (voidFloat1 ~~.-5 voidFloat2) { // The negative sign at the other end signals reversion (13.3 ~~.1 17.6 == 13.3 -1.~~ 17.6)
            print("yay3")
        }
    }

    final int[] i = (Integer[]) [0].toArray()

    string SampleString(string name) { // Allows nested functions
        s = f"Hello, {name}! How are you today my little fella? The i is: {i[0]}" // Fstrings, also the scope for any variable like i is inferred via bottom-up, previously defined search

        i[0] = 1

        return s
        /*
        A bit of clarity on scope:
        {
            i = 1
            i = 2 <- selected

            print(i) // Same scope, latest definition

            i = 3
        }
        {
            i = 1

            void print_() {
                i = 2 <- selected

                print(i) // Local scope, up-closest neighbour first

                i = 3
            }
        }
        {
            i = 1 <- selected

            void print_() {
                print(i) // Upper scope, up-closest neighbour first

                i = 2
                i = 3
            }
        }
        */
    }

    string sampleString = f"{SampleString("random user")}\ntest"

    print(sampleString)
}

var varFunction1() { // Flux also allows loosely typed functions, it inherits the type from the return statement, ex. int
    return 1
}

varFunction2() { // Function type will resolve into illegal if there are no returns, or any returns are of different types, this one is legal and is double
    if (bool1) {
        return [1.0 for i in range(1000)][0] // Python-style generators
    }
    else {
        [{ print(i) } for i in range(1000)] // We even have slightly more advanced than python generators in java

        return 1.0 + 1 // Handles auto promotion (double + int -> double)
    }
}

unfinished void unfinishedVoid() { // Flux also has a special comment function or variable modifier for unfinished parts of your code

    illegal float illegalFloat; // I think you can find a few places this could be useful in

    unpolished void unpolishedVoid() { print(5 *** 5) } // For when something you coded works fine, but could be rewritten by a more competent person
    suboptimal void suboptimalVoid() { for i in range(1000000) { print(i) } } // This could signal logic that works great at the expense of the execution time
    ugly void uglyVoid() { [x for x in range(100)][0] } // This one just begs to be rewritten because of how ugly you found it

}

exp1 = (float) 2 ** 3 ** 4 // Operations handled according to associativity from this list (ordered by the lexer precedence):
/*
(This will be moved to the documentation eventually)

    Tetration -> Right to Left
        expression *** expression

    Exponentiation -> Right to Left
        expression ** expression

    Array access -> Left to Right
        expression[expression]

    Lambda functions -> Right to Left
        lambda id...: expression
        (id...) -> or => expression

    Walrus operator -> Right to Left
        id := expression

    Approximation -> Left to Right
        expression ~~ expression // Default precision is 1
        expression int.~~ expression
        expression ~~.int expression

    Variable access -> Left to Right
        expression.expression

    Casting -> Left to Right
        (type) expression

    Not operator -> Left to Right // Because not should behave like ! does, yet this might be changed in the future
        !expression
        not(expression)

    Multiplication/Division -> Left to Right
        expression * or / or % or mod expression

    Floor Division -> Left to Right
        expression /% expression
        expression floor expression

    Ceil Division -> Left to Right
        expression %/ expression
        expression ceil expression

    Addition/Subtraction -> Left to Right
        expression + or - expression

    Bit Shift operations -> Left to Right
        expression << or >> or >>> expression

    Comparisons -> Left to Right unless Chained
        expression < or > or <= or >= expression
        expression instanceof expression

    Equality -> Left to Right
        expression == or != expression

    Bitwise operations -> Left to Right
        expression & or ^ or | expression

    Logical operations -> Left to Right
        expression && or and expression
        expression || or 'or' expression
*/