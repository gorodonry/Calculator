import java.util.List;
import java.util.Map;

/**
 * Contains methods for applying mathematical operations to lists of numbers.
 */
public final class CMath {
    /**
     * A map of mathematical operations keyed to the order they are applied (BEDMAS).
     */
    public static final Map<String, OperationOrder> operations = Map.ofEntries(
            Map.entry("+", OperationOrder.AdditionSubtraction),
            Map.entry("-", OperationOrder.AdditionSubtraction),
            Map.entry("*", OperationOrder.DivisionMultiplication),
            Map.entry("/", OperationOrder.DivisionMultiplication),
            Map.entry("^", OperationOrder.Exponents),
            Map.entry("sqrt", OperationOrder.BracketsFunctions),
            Map.entry("ln", OperationOrder.BracketsFunctions),
            Map.entry("sin", OperationOrder.BracketsFunctions),
            Map.entry("cos", OperationOrder.BracketsFunctions),
            Map.entry("tan", OperationOrder.BracketsFunctions),
            Map.entry("csc", OperationOrder.BracketsFunctions),
            Map.entry("sec", OperationOrder.BracketsFunctions),
            Map.entry("cot", OperationOrder.BracketsFunctions)
    );

    /**
     * Accuracy threshold for comparing doubles.
     */
    public static final double EPSILON = 0.000001;

    /**
     * Compares two doubles using a threshold accuracy value ({@link CMath#EPSILON}).
     *
     * @param d1 the first double to compare.
     * @param d2 the second double to compare.
     *
     * @return true if the absolute difference between the doubles is negligible, false otherwise.
     */
    public static boolean compareDouble(double d1, double d2) {
        return Math.abs(d1 - d2) < EPSILON;
    }

    /**
     * Adds all the elements of the given list together.
     *
     * @param elements the list of elements to add together.
     *
     * @return the sum of the elements in the list.
     */
    public static Result add(List<Double> elements) {
        if (elements.isEmpty()) {
            return new UnsuccessfulResult("No operands provided for +");
        }

        return new SuccessfulResult(elements.stream().mapToDouble(d -> d).sum());
    }

    /**
     * Subtracts the sum of all elements beyond index 1 of the supplied list from the first element in the list.
     *
     * @param elements the list of elements to perform the subtraction with.
     *
     * @return the first element if the size of the list is 1, otherwise the result of subtracting the sum of all
     *         elements beyond index 1 from the first element.
     */
    public static Result subtract(List<Double> elements) {
        if (elements.isEmpty()) {
            return new UnsuccessfulResult("No operands provided for -");
        }

        return new SuccessfulResult(
                elements.getFirst() - add(elements.subList(1, elements.size())).result().orElse(0.)
        );
    }

    /**
     * Multiplies all the elements of the given list together.
     *
     * @param elements the list of elements to multiply together.
     *
     * @return the product of the elements in the list.
     */
    public static Result multiply(List<Double> elements) {
        if (elements.isEmpty()) {
            return new UnsuccessfulResult("No operands provided for *");
        }

        return new SuccessfulResult(elements.stream().reduce((x, y) -> x * y).get());
    }

    /**
     * Divides the first element in the list by the product of all remaining elements.
     *
     * @param elements the list of elements to perform the division with.
     *
     * @return the first element if the size of the list is 1, otherwise the result of dividing the first element by
     *         the product of all remaining elements in the list.
     */
    public static Result divide(List<Double> elements) {
        if (elements.isEmpty()) {
            return new UnsuccessfulResult("No operands provided for /");
        }

        return new SuccessfulResult(
                elements.getFirst() / multiply(elements.subList(1, elements.size())).result().orElse(0.)
        );
    }

    /**
     * Raises the first element to the power of the second element.
     *
     * @param elements the list of elements to perform the exponent calculation with.
     *
     * @return the result of raising the first element to the power of the second element if two operands were provided,
     *         otherwise an unsuccessful result is returned with an accompanying message.
     */
    public static Result calculateExponent(List<Double> elements) {
        if (elements.size() != 2) {
            return new UnsuccessfulResult("Invalid number of operands for ^ (required operands: 2)");
        }

        return new SuccessfulResult(Math.pow(elements.get(0), elements.get(1)));
    }

    /**
     * If n is the second element in the list, then this function calculates the nth root of the first element.
     *  If no second element is provided, then the square root is calculated.
     *
     * @param elements the list of elements to perform the root calculation with.
     *
     * @return the nth root of the first element, where n is the second element (or 2 if no second element was provided).
     */
    public static Result calculateRoot(List<Double> elements) {
        return switch (elements.size()) {
            case 1 -> new SuccessfulResult(Math.sqrt(elements.getFirst()));
            case 2 -> new SuccessfulResult(Math.pow(elements.getFirst(), 1 / elements.get(1)));
            default -> new UnsuccessfulResult("Invalid number of operands for root (required operands: 1 or 2)");
        };
    }

    /**
     * Calculates the logarithm of the first element in the given list to the base of the second element. If a second
     *  element is not provided base e is assumed.
     *
     * @param elements the list of elements to perform the logarithmic calculation with.
     *
     * @return the logarithm of the first element to the base of the second element (base 10 assumed if no second
     *         element provided). If neither 1 nor 2 elements are provided returns an unsuccessful result with an
     *         accompanying message.
     */
    public static Result calculateLogarithm(List<Double> elements) {
        return switch (elements.size()) {
            case 1 -> new SuccessfulResult(Math.log(elements.getFirst()));
            case 2 -> new SuccessfulResult(Math.log(elements.get(0)) / Math.log(elements.get(1)));
            default -> new UnsuccessfulResult("Invalid number of operands for log (required operands: 1 or 2)");
        };
    }

    /**
     * Applies the sine function to the first element of the given list.
     *
     * @param elements the list of elements to apply the sine function to.
     *
     * @return the result of the sine function on the first element in the list, or an unsuccessful result if exactly 1
     *         element isn't provided.
     */
    public static Result calculateSin(List<Double> elements) {
        if (elements.size() != 1) {
            return new UnsuccessfulResult("Invalid number of operands for sin (required operands: 1)");
        }

        return new SuccessfulResult(Math.sin(elements.getFirst()));
    }

    /**
     * Applies the cosine function to the first element of the given list.
     *
     * @param elements the list of elements to apply the cosine function to.
     *
     * @return the result of the cosine function on the first element in the list, or an unsuccessful result if exactly
     *         1 element isn't provided.
     */
    public static Result calculateCos(List<Double> elements) {
        if (elements.size() != 1) {
            return new UnsuccessfulResult("Invalid number of operands for cos (required operands: 1)");
        }

        return new SuccessfulResult(Math.cos(elements.getFirst()));
    }

    /**
     * Applies the tangent function to the first element of the given list.
     *
     * @param elements the list of elements to apply the tangent function to.
     *
     * @return the result of the tangent function on the first element in the list, or an unsuccessful result if exactly
     *         1 element isn't provided.
     */
    public static Result calculateTan(List<Double> elements) {
        if (elements.size() != 1) {
            return new UnsuccessfulResult("Invalid number of operands for tan (required operands: 1)");
        }

        return new SuccessfulResult(Math.tan(elements.getFirst()));
    }

    /**
     * Applies the cosecant function to the first element of the given list.
     *
     * @param elements the list of elements to apply the cosecant function to.
     *
     * @return the result of the cosecant function on the first element in the list, or an unsuccessful result if
     *         exactly 1 element isn't provided.
     */
    public static Result calculateCsc(List<Double> elements) {
        if (elements.size() != 1) {
            return new UnsuccessfulResult("Invalid number of operands for csc (required operands: 1");
        }

        return new SuccessfulResult(1 / calculateSin(elements).result().orElseThrow());
    }

    /**
     * Applies the secant function to the first element of the given list.
     *
     * @param elements the list of elements to apply the secant function to.
     *
     * @return the result of the secant function on the first element in the list, or an unsuccessful result if
     *         exactly 1 element isn't provided.
     */
    public static Result calculateSec(List<Double> elements) {
        if (elements.size() != 1) {
            return new UnsuccessfulResult("Invalid number of operands for sec (required operands: 1");
        }

        return new SuccessfulResult(1 / calculateCos(elements).result().orElseThrow());
    }

    /**
     * Applies the cotangent function to the first element of the given list.
     *
     * @param elements the list of elements to apply the cotangent function to.
     *
     * @return the result of the cotangent function on the first element in the list, or an unsuccessful result if
     *         exactly 1 element isn't provided.
     */
    public static Result calculateCot(List<Double> elements) {
        if (elements.size() != 1) {
            return new UnsuccessfulResult("Invalid number of operands for cot (required operands: 1");
        }

        return new SuccessfulResult(1 / calculateTan(elements).result().orElseThrow());
    }

    /**
     * Calculates the average of a list of elements.
     *
     * @param elements the list of elements to calculate the average of.
     *
     * @return the average of the given list of elements.
     */
    public static Result calculateAverage(List<Double> elements) {
        if (elements.isEmpty()) {
            return new UnsuccessfulResult("No operands provided for avg");
        }

        return new SuccessfulResult(add(elements).result().orElseThrow() / elements.size());
    }

    /**
     * Calculates the Euclidean distance for a given list of coordinates.
     *
     * @param coords the list of coordinates in the form [x1, y1, z1, ..., t1, x2, y2, z2, ..., t2].
     *
     * @return the Euclidean distance of the given coordinates, or Double.NaN if an uneven number of coordinates are
     *         provided.
     */
    public static Result calculateEuclideanDistance(List<Double> coords) {
        if (coords.size() % 2 != 0) {
            return new UnsuccessfulResult("Invalid number of operands for dist (must be an even number)");
        }

        double distance = 0;
        for (int i = 0; i < coords.size() / 2; i++) {
            distance += Math.pow(coords.get(i) - coords.get((coords.size() / 2) + i), 2);
        }

        return new SuccessfulResult(Math.sqrt(distance));
    }
}
