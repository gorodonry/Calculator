import java.util.List;
import java.util.Map;

/**
 * Contains methods for applying mathematical operations to lists of numbers.
 */
public abstract class CMath {
    /**
     * A map of operations keyed to the order they occur in (BEDMAS).
     */
    public static Map<String, OperationOrder> OPERATION_ORDERS = Map.ofEntries(
            Map.entry("+", OperationOrder.ADDITION_SUBTRACTION),
            Map.entry("-", OperationOrder.ADDITION_SUBTRACTION),
            Map.entry("*", OperationOrder.DIVISION_MULTIPLICATION),
            Map.entry("/", OperationOrder.DIVISION_MULTIPLICATION),
            Map.entry("^", OperationOrder.EXPONENTS),
            Map.entry("sqrt", OperationOrder.BRACKETS_FUNCTIONS),
            Map.entry("ln", OperationOrder.BRACKETS_FUNCTIONS),
            Map.entry("sin", OperationOrder.BRACKETS_FUNCTIONS),
            Map.entry("cos", OperationOrder.BRACKETS_FUNCTIONS),
            Map.entry("tan", OperationOrder.BRACKETS_FUNCTIONS),
            Map.entry("csc", OperationOrder.BRACKETS_FUNCTIONS),
            Map.entry("sec", OperationOrder.BRACKETS_FUNCTIONS),
            Map.entry("cot", OperationOrder.BRACKETS_FUNCTIONS)
    );

    /**
     * Adds all the elements of the given list together.
     * @param elements the list of elements to add together.
     * @return the sum of the elements in the list.
     */
    public static Result add(List<Double> elements) {
        if (elements.isEmpty())
            return new UnsuccessfulResult("No operands provided for +");

        return new SuccessfulResult(elements.stream().mapToDouble(d -> d).sum());
    }

    /**
     * Subtracts the sum of all elements beyond index 1 of the supplied list from the first element in the list.
     * @param elements the list of elements to perform the subtraction with.
     * @return the first element if the size of the list is 1, otherwise the result of subtracting the sum of all
     *  elements beyond index 1 from the first element.
     */
    public static Result subtract(List<Double> elements) {
        if (elements.isEmpty())
            return new UnsuccessfulResult("No operands provided for -");

        if (elements.size() == 1)
            return new SuccessfulResult(elements.get(0));

        return new SuccessfulResult(elements.get(0) - add(elements.subList(1, elements.size())).result());
    }

    /**
     * Multiplies all the elements of the given list together.
     * @param elements the list of elements to multiply together.
     * @return the product of the elements in the list.
     */
    public static Result multiply(List<Double> elements) {
        if (elements.isEmpty())
            return new UnsuccessfulResult("No operands provided for *");

        return new SuccessfulResult(elements.stream().reduce((x, y) -> x * y).get());
    }

    /**
     * Divides the first element in the list by the product of all remaining elements.
     * @param elements the list of elements to perform the division with.
     * @return the first element if the size of the list is 1, otherwise the result of dividing the first element by
     *  the product of all remaining elements in the list.
     */
    public static Result divide(List<Double> elements) {
        if (elements.isEmpty())
            return new UnsuccessfulResult("No operands provided for /");

        if (elements.size() == 1)
            return new SuccessfulResult(elements.get(0));

        return new SuccessfulResult(elements.get(0) / multiply(elements.subList(1, elements.size())).result());
    }

    /**
     * Raises the first element to the power of the second element.
     * @param elements the list of elements to perform the exponent calculation with.
     * @return the result of raising the first element to the power of the second element if two operands were provided,
     *  otherwise an unsuccessful result is returned with an accompanying message.
     */
    public static Result calculateExponent(List<Double> elements) {
        if (elements.size() != 2)
            return new UnsuccessfulResult("Invalid number of operands for ^ (required operands: 2)");

        return new SuccessfulResult(Math.pow(elements.get(0), elements.get(1)));
    }

    /**
     * Calculates the specified root of the given number.
     * @param number the number to calculate the root of.
     * @param root the root of the number to calculate.
     * @return the specified root of the given number.
     */
    public static Result calculateRoot(double number, double root) {
        return new SuccessfulResult(Math.pow(number, 1 / root));
    }

    /**
     * Calculates the square root of the first element in the given list.
     * @param elements the list of elements to perform the square root calculation with.
     * @return the square root of the first element of the list, or an unsuccessful result with an accompanying message
     *  if exactly 1 element was not provided.
     */
    public static Result calculateSqrt(List<Double> elements) {
        if (elements.size() != 1)
            return new UnsuccessfulResult("Invalid number of operands for sqrt (required operands: 1)");

        return new SuccessfulResult(Math.sqrt(elements.get(0)));
    }

    /**
     * Calculates the logarithm of the first element in the given list to the base of the second element. If a second
     *  element is not provided base 10 is assumed.
     * @param elements the list of elements to perform the logarithmic calculation with.
     * @return the logarithm of the first element to the base of the second element (base 10 assumed if no second
     *  element provided). If neither 1 nor 2 elements are provided returns an unsuccessful result with an accompanying
     *  message.
     */
    public static Result calculateLogarithm(List<Double> elements) {
        switch (elements.size()) {
            case 1 -> {
                return new SuccessfulResult(Math.log10(elements.get(0)));
            }
            case 2 -> {
                return new SuccessfulResult(Math.log(elements.get(0)) / Math.log(elements.get(1)));
            }
            default -> {
                return new UnsuccessfulResult("Invalid number of operands for log (required operands: 1 or 2)");
            }
        }
    }

    /**
     * Applies the natural logarithm function to the first element of the given list.
     * @param elements the list of elements to apply the natural logarithm function to.
     * @return the result of the natural logarithm function on the first element in the list, or an unsuccessful result
     *  if exactly 1 element isn't provided.
     */
    public static Result calculateNaturalLogarithm(List<Double> elements) {
        if (elements.size() != 1)
            return new UnsuccessfulResult("Invalid number of operands for ln (required operands: 1)");

        return new SuccessfulResult(Math.log(elements.get(0)));
    }

    /**
     * Applies the sine function to the first element of the given list.
     * @param elements the list of elements to apply the sine function to.
     * @return the result of the sine function on the first element in the list, or an unsuccessful result if exactly 1
     *  element isn't provided.
     */
    public static Result calculateSin(List<Double> elements) {
        if (elements.size() != 1)
            return new UnsuccessfulResult("Invalid number of operands for sin (required operands: 1)");

        return new SuccessfulResult(Math.sin(elements.get(0)));
    }

    /**
     * Applies the cosine function to the first element of the given list.
     * @param elements the list of elements to apply the cosine function to.
     * @return the result of the cosine function on the first element in the list, or an unsuccessful result if exactly
     *  1 element isn't provided.
     */
    public static Result calculateCos(List<Double> elements) {
        if (elements.size() != 1)
            return new UnsuccessfulResult("Invalid number of operands for cos (required operands: 1)");

        return new SuccessfulResult(Math.cos(elements.get(0)));
    }

    /**
     * Applies the tangent function to the first element of the given list.
     * @param elements the list of elements to apply the tangent function to.
     * @return the result of the tangent function on the first element in the list, or an unsuccessful result if exactly
     *  1 element isn't provided.
     */
    public static Result calculateTan(List<Double> elements) {
        if (elements.size() != 1)
            return new UnsuccessfulResult("Invalid number of operands for tan (required operands: 1)");

        return new SuccessfulResult(Math.tan(elements.get(0)));
    }

    /**
     * Applies the cosecant function to the first element of the given list.
     * @param elements the list of elements to apply the cosecant function to.
     * @return the result of the cosecant function on the first element in the list, or an unsuccessful result if
     *  exactly 1 element isn't provided.
     */
    public static Result calculateCsc(List<Double> elements) {
        if (elements.size() != 1)
            return new UnsuccessfulResult("Invalid number of operands for csc (required operands: 1");

        return new SuccessfulResult(1 / calculateSin(elements).result());
    }

    /**
     * Applies the secant function to the first element of the given list.
     * @param elements the list of elements to apply the secant function to.
     * @return the result of the secant function on the first element in the list, or an unsuccessful result if
     *  exactly 1 element isn't provided.
     */
    public static Result calculateSec(List<Double> elements) {
        if (elements.size() != 1)
            return new UnsuccessfulResult("Invalid number of operands for sec (required operands: 1");

        return new SuccessfulResult(1 / calculateCos(elements).result());
    }

    /**
     * Applies the cotangent function to the first element of the given list.
     * @param elements the list of elements to apply the cotangent function to.
     * @return the result of the cotangent function on the first element in the list, or an unsuccessful result if
     *  exactly 1 element isn't provided.
     */
    public static Result calculateCot(List<Double> elements) {
        if (elements.size() != 1)
            return new UnsuccessfulResult("Invalid number of operands for cot (required operands: 1");

        return new SuccessfulResult(1 / calculateTan(elements).result());
    }

    /**
     * Calculates the average of a list of elements.
     * @param elements the list of elements to calculate the average of.
     * @return the average of the given list of elements.
     */
    public static Result calculateAvg(List<Double> elements) {
        if (elements.isEmpty())
            return new UnsuccessfulResult("No operands provided for avg");

        return new SuccessfulResult(add(elements).result() / elements.size());
    }

    /**
     * Calculates the Euclidean distance for a given list of coordinates.
     * @param coords the list of coordinates in the form [x1, y1, z1, ..., t1, x2, y2, z2, ..., t2].
     * @return the Euclidean distance of the given coordinates, or Double.NaN if an uneven number of coordinates are
     *  provided.
     */
    public static Result calculateEuclideanDistance(List<Double> coords) {
        if (coords.size() % 2 != 0)
            return new UnsuccessfulResult("Invalid number of operands for dist (must be an even number)");

        double distance = 0;
        for (int i = 0; i < coords.size() / 2; i++)
            distance += Math.pow(coords.get(i) - coords.get((coords.size() / 2) + i), 2);

        return new SuccessfulResult(Math.sqrt(distance));
    }
}
