/**
 * Contains various methods that provide information to the user about syntax, quitting, etc.
 */
public final class CHelp {
    /**
     * Prints out documentation of supported commands and how to use them.
     */
    public static void help() {
        System.out.print("""
A simple calculator written in Java that supports standard mathematical notation.

--General Notes--
 -> Decimals supported up to a maximum of 10dp.
 -> Supports pi entered as pi e.g. '5pi' for 5 times pi.
 -> Supports e entered as e e.g. '5e^4' for 5 times e to the power of 4.
 -> Supports brackets and applies BEDMAS automatically, brackets can be different types; ( and [ are both accepted.
 -> Supports comparisons using = e.g. '5 = 7 - 2' will yield 'true'.
 -> You can enter 'q' to quit :)

--Basic Operators--
 -> 'x + y': adds x and y.
 -> 'x - y': subtracts y from x.
 -> 'x * y': multiplies x and y.
 -> 'x / y': divides x by y.
 -> 'x ^ y': raises x to the power of y.

--Trigonometric Functions--
Note the default for all trigonometric functions is radians.
 -> sin(x): returns the sine of x.
 -> cos(x): returns the cosine of x.
 -> tan(x): returns the tangent of x (x ≠ π/2).
 -> csc(x): returns the cosecant of x.
 -> sec(x): returns the secant of x.
 -> cot(x): returns the cotangent of x (x ≠ π/2).

--Other Functions--
ln(x): returns the natural logarithm of x (x > 0).

--Other Notes--
Sorry for writing this in Java I'll do better next time.
""");
    }
}
