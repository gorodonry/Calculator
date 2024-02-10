import java.util.*;
import java.util.stream.Collectors;
import static java.util.Map.entry;

/**
 * A calculator program.
 */
public class Calculator {
    boolean running = true;
    List<String> expressionErrorMessages = new ArrayList<>();

    Scanner reader;

    /**
     * Initialises the program. <a href="https://stackoverflow.com/questions/14142853/close-a-scanner-linked-to-system-in">Feast your eyes...</a>
     */
    public void initialise() {
        reader = new Scanner(System.in);
        runCalculator();
    }

    /**
     * Terminates the program.
     */
    public void terminate() {
        reader.close();
    }

    /**
     * Runs the calculator until the user doesn't want any more problems solved.
     */
    public void runCalculator() {
        System.out.println("A simple calculator created in (I'm sorry) Java. You can enter 'q' to quit :)");
        while (running) {
            System.out.println();
            expressionErrorMessages = new ArrayList<>();

            Optional<List<String>> input = readExpression();

            if (!running) {
                continue;
            }

            System.out.println("Read expression");

            if (input.isPresent()) {
                System.out.println("Hi-ho");
                System.out.println(input.get());
                Result result = evaluate(input.get());

                if (result.successful()) {
                    System.out.printf(" -> %f%n", result.result());
                } else {
                    if (result.errorMessage() != null) {
                        expressionErrorMessages.add(result.errorMessage());
                    }

                    System.out.println("Failed to solve expression due to the following reason(s):");

                    for (String error : expressionErrorMessages) {
                        System.out.printf(" -> %s%n", error);
                    }
                }
            } else {
                System.out.println("Failed to interpret input due to the following reason(s):");

                for (String error : expressionErrorMessages) {
                    System.out.printf(" -> %s%n", error);
                }
            }
        }

        terminate();
    }

    /**
     * Evaluates an interpreted (grouped by numbers and commands) expression.
     * @param expression the expression to solve.
     * @return the answer to the expression (provided no errors arise while attempting to solve).
     */
    public Result evaluate(List<String> expression) {
        // Locate and solve the contents of the rightmost opening bracket until no brackets are remaining, then solve
        //  anything that remains. Applies BEDMAS.

        while (expression.contains("(") || expression.contains("[")) {
            int openBracketIndex = -1;
            for (int i = 0; i < expression.size(); i++) {
                if (expression.get(i).equals("(") || expression.get(i).equals("[")) {
                    openBracketIndex = i;
                }
            }

            int closeBracketIndex = -1;
            for (int i = openBracketIndex + 1; i < expression.size(); i++) {
                // Note that due to prior checking of the input we can assume the brackets match.
                if (expression.get(i).equals(")") || expression.get(i).equals("]")) {
                    closeBracketIndex = i;
                    break;
                }
            }

            Result result = evaluate(expression.subList(openBracketIndex + 1, closeBracketIndex));

            if (!result.successful()) {
                return new UnsuccessfulResult(null);
            }

            expression.subList(openBracketIndex, openBracketIndex + 3).clear();
            expression.add(openBracketIndex, String.valueOf(result.result()));
        }

        // Mo brackets present; evaluate the expression using (B)EDMAS.

        Map<OperationOrder, List<Integer>> operationsToCompute = Map.ofEntries(
                entry(OperationOrder.BRACKETS_FUNCTIONS, new ArrayList<>()),
                entry(OperationOrder.EXPONENTS, new ArrayList<>()),
                entry(OperationOrder.DIVISION_MULTIPLICATION, new ArrayList<>()),
                entry(OperationOrder.ADDITION_SUBTRACTION, new ArrayList<>())
        );

        for (int i = 0; i < expression.size(); i++) {
            if (CMath.OPERATION_ORDERS.containsKey(expression.get(i))) {
                operationsToCompute.get(CMath.OPERATION_ORDERS.get(expression.get(i))).add(i);
            }
        }

        // Compute all the functions from rightmost to leftmost.

        Stack<Integer> functionIndices = new Stack<>();
        functionIndices.addAll(operationsToCompute.get(OperationOrder.BRACKETS_FUNCTIONS));

        while (!functionIndices.isEmpty()) {
            int index = functionIndices.pop();

            if (index == expression.size() - 1) {
                expressionErrorMessages.add(String.format("No argument supplied for %s function",
                        expression.get(index)));
                break;
            }

            List<Double> argument;
            try {
                argument = List.of(Double.parseDouble(expression.get(index + 1)));
            } catch (NumberFormatException ignored) {
                expressionErrorMessages.add(String.format("%s is not a number...", expression.get(index + 1)));
                break;
            }

            Result result = switch (expression.get(index)) {
                case "sqrt" -> CMath.calculateSqrt(argument);
                case "ln" -> CMath.calculateNaturalLogarithm(argument);
                case "sin" -> CMath.calculateSin(argument);
                case "cos" -> CMath.calculateCos(argument);
                case "tan" -> CMath.calculateTan(argument);
                case "csc" -> CMath.calculateCsc(argument);
                case "sec" -> CMath.calculateSec(argument);
                case "cot" -> CMath.calculateCot(argument);
                default -> new UnsuccessfulResult("Please contact the dev, this shouldn't have happened");
            };

            if (!result.successful()) {
                expressionErrorMessages.add(result.errorMessage());
                break;
            }

            expression.subList(index, index + 2).clear();
            expression.add(index, String.valueOf(result.result()));

            shuntOperationIndices(operationsToCompute, index, 1);
        }

        if (!expressionErrorMessages.isEmpty()) {
            return new UnsuccessfulResult(null);
        }

        computeBinaryOperations(operationsToCompute, OperationOrder.EXPONENTS, expression);

        if (!expressionErrorMessages.isEmpty()) {
            return new UnsuccessfulResult(null);
        }

        computeBinaryOperations(operationsToCompute, OperationOrder.DIVISION_MULTIPLICATION, expression);

        if (!expressionErrorMessages.isEmpty()) {
            return new UnsuccessfulResult(null);
        }

        computeBinaryOperations(operationsToCompute, OperationOrder.ADDITION_SUBTRACTION, expression);

        if (!expressionErrorMessages.isEmpty()) {
            return new UnsuccessfulResult(null);
        }

        if (expression.size() != 1) {
            return new UnsuccessfulResult("No more operators to apply to the remaining numbers");
        }

        return new SuccessfulResult(Double.parseDouble(expression.get(0)));
    }

    /**
     * Calculates all binary operations found of a particular order in (B)EDMAS from left to right.
     * @param allOperationIndices the indices of all binary operations sorted by the order they need to be solved.
     * @param operationTypesToCompute the types of operation to compute with this particular use of the function.
     * @param partiallySolvedExpression the partially solved expression.
     */
    private void computeBinaryOperations(Map<OperationOrder, List<Integer>> allOperationIndices,
                                           OperationOrder operationTypesToCompute,
                                           List<String> partiallySolvedExpression) {
        while (!allOperationIndices.get(operationTypesToCompute).isEmpty()) {
            int index = allOperationIndices.get(operationTypesToCompute).removeFirst();

            if (index == 0 || index == partiallySolvedExpression.size() - 1) {
                expressionErrorMessages.add(String.format("Not enough arguments (requires 2) supplied for %s",
                        partiallySolvedExpression.get(index)));
                break;
            }

            List<Double> arguments = new ArrayList<>();

            try {
                arguments.add(Double.parseDouble(partiallySolvedExpression.get(index - 1)));
                arguments.add(Double.parseDouble(partiallySolvedExpression.get(index + 1)));
            } catch (NumberFormatException ignored) {
                String incorrectComponent;

                if (arguments.size() == 1) {
                    incorrectComponent = partiallySolvedExpression.get(index + 1);
                } else {
                    incorrectComponent = partiallySolvedExpression.get(index - 1);
                }

                expressionErrorMessages.add(String.format("%s is not a number...", incorrectComponent));
                break;
            }

            Result result = switch (partiallySolvedExpression.get(index)) {
                case "^" -> CMath.calculateExponent(arguments);
                case "*" -> CMath.multiply(arguments);
                case "/" -> CMath.divide(arguments);
                case "+" -> CMath.add(arguments);
                case "-" -> CMath.subtract(arguments);
                default -> new UnsuccessfulResult("Please contact the dev; this shouldn't have happened");
            };

            if (!result.successful()) {
                expressionErrorMessages.add(result.errorMessage());
                break;
            }

            partiallySolvedExpression.subList(index - 1, index + 2).clear();
            partiallySolvedExpression.add(index - 1, String.valueOf(result.result()));
            shuntOperationIndices(allOperationIndices, index, 2);
        }
    }

    /**
     * Shunts known operation indices by a certain factor. Useful after just computing an operation that combines, say,
     *  5 + 6 (3 entries in the expression list) into 11 (1 entry in the expression list) thus shunting all known
     *  operation indices beyond the + operator down by 2.
     * @param operationsToComplete the known indices of operators in the expression not yet computed.
     * @param completedOperationIndex the index of the operator just computed.
     * @param shuntBy the factor to shunt all known operation indices by.
     */
    private void shuntOperationIndices(Map<OperationOrder, List<Integer>> operationsToComplete,
                                       int completedOperationIndex,
                                       int shuntBy) {
        for (OperationOrder key : operationsToComplete.keySet()) {
            List<Integer> operationIndices = operationsToComplete.get(key);
            for (int i = 0; i < operationIndices.size(); i++) {
                if (operationIndices.get(i) > completedOperationIndex) {
                    int index = operationIndices.remove(i);
                    operationIndices.add(i, index - shuntBy);
                }
            }
        }
    }

    /**
     * Obtains a string from the user representing the expression they would like solved, and then checks and formats
     *  it.
     * @return a list of components representing the expression, or nothing if errors were encountered while
     *  interpreting the expression.
     */
    public Optional<List<String>> readExpression() {
        System.out.print("Expression: ");
        String expression = reader.nextLine().trim().toLowerCase();

        if (expression.equals("q")) {
            running = false;
            return Optional.empty();
        }

        if (expression.isBlank()) {
            expressionErrorMessages.add("Try entering something...");
        }

        if (!MathReader.bracketsMatching(expression)) {
            expressionErrorMessages.add("Brackets do not match");
        }

        if (MathReader.containsEmptyBrackets(expression)) {
            expressionErrorMessages.add("Expression contains empty brackets");
        }

        if (!expressionErrorMessages.isEmpty()) {
            return Optional.empty();
        }

        return MathReader.readExpression(expression.chars()
                .mapToObj(c -> String.format("%c", (char)c))
                .filter(s -> !s.equals(" "))
                .collect(Collectors.toList()),
                expressionErrorMessages);
    }

    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        calculator.initialise();
    }
}
