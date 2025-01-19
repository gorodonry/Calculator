import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import static java.util.Map.entry;

/**
 * A simple and not particularly well written calculator program.
 */
public class Calculator {
    static final char[] USER_DEFINABLE_CONSTANTS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    static final DecimalFormat decimalFormat = new DecimalFormat("#.##########");
    static final List<String> nonCalculatorInputs = List.of("q", "help");

    boolean running = true;
    String currentExpression;
    List<String> expressionErrorMessages = new ArrayList<>();
    List<Query> history = new ArrayList<>();
    Map<String, Optional<Double>> constants = new HashMap<>();
    Scanner reader;

    /**
     * Initialises the program. <a href="https://stackoverflow.com/questions/14142853/close-a-scanner-linked-to-system-in">Feast your eyes...</a>
     */
    public void initialise() {
        reader = new Scanner(System.in);

        // Initialise user-defined constants as empty optionals.
        for (char constant : USER_DEFINABLE_CONSTANTS) {
            constants.put(String.valueOf(constant), Optional.empty());
        }

        constants.put("ans", Optional.empty());
        constants.put("e", Optional.of(Math.E));
        constants.put("pi", Optional.of(Math.PI));

        runProgram();
    }

    /**
     * Terminates the program.
     */
    public void terminate() {
        reader.close();
    }

    /**
     * Main loop of the program; once called it prompts for input until terminated by the user.
     */
    public void runProgram() {
        System.out.println("A simple calculator written in Java. You can enter 'q' to quit :)");

        while (running) {
            System.out.println();
            expressionErrorMessages.clear();

            if (!readExpression()) {
                reportError("interpret");
                return;
            }

            if (nonCalculatorInputs.contains(currentExpression)) {
                switch (currentExpression) {
                    case "q" -> running = false;
                    case "help" -> CHelp.help();
                    default -> { }
                }
            } else {
                runCalculator();
            }
        }

        terminate();
    }

    /**
     * Runs the calculator solver with the current saved expression.
     */
    void runCalculator() {
        if (currentExpression.contains("=")) {
            reportComparison();
        } else {
            reportExpression();
        }
    }

    /**
     * Evaluates the expression currently saved in this class (see {@link Calculator#currentExpression}).
     */
    void reportExpression() {
        Optional<List<String>> interpretedExpression = readExpression(currentExpression);

        if (interpretedExpression.isPresent()) {
            Result result = evaluate(interpretedExpression.get());

            if (result.successful()) {
                System.out.printf(" -> %s%n", decimalFormat.format(result.result().orElseThrow()));
                history.add(new Query(currentExpression, result.result().orElseThrow()));
                constants.put("ans", Optional.of(result.result().orElseThrow()));
            } else {
                result.errorMessage().ifPresent(e -> expressionErrorMessages.add(e));
                reportError("solve");
            }
        } else {
            reportError("interpret");
        }
    }

    /**
     * Evaluates whether two expressions separated by an equals sign are equal to each other. Note that the string
     *  containing the expressions to evaluate is {@link Calculator#currentExpression}.
     */
    void reportComparison() {
        if (currentExpression.chars().filter(c -> c == '=').count() > 1) {
            expressionErrorMessages.add("More than one equals sign entered");
            reportError("interpret");
            return;
        }

        Optional<List<String>> leftHandSide = readExpression(currentExpression.split("=")[0]);
        Optional<List<String>> rightHandSide = readExpression(currentExpression.split("=")[1]);

        Result leftHandResult = evaluate(leftHandSide.orElseGet(ArrayList::new));
        Result rightHandResult = evaluate(rightHandSide.orElseGet(ArrayList::new));

        if (leftHandResult.successful() && rightHandResult.successful()) {
            System.out.printf(
                    " -> %s%n",
                    CMath.compareDouble(
                            leftHandResult.result().orElseThrow(),
                            rightHandResult.result().orElseThrow()
                    )
            );
        } else {
            leftHandResult.errorMessage().ifPresent(e -> expressionErrorMessages.add(e));
            rightHandResult.errorMessage().ifPresent(e -> expressionErrorMessages.add(e));
            reportError("solve");
        }
    }

    /**
     * Attempts to parse an expression using {@link MathReader#readExpression}. Any errors encountered are
     *  recorded in {@link Calculator#expressionErrorMessages}.
     *
     * @param expression the expression to read.
     *
     * @return a list of expression components if the parse was successful, an empty optional otherwise.
     */
    Optional<List<String>> readExpression(String expression) {
        return MathReader.readExpression(
                expression.chars()
                        .mapToObj(c -> String.valueOf((char) c))
                        .collect(Collectors.toList()),
                expressionErrorMessages,
                constants
        );
    }

    /**
     * Reports any errors that occurred while attempting an action.
     *
     * @param failedAction the action that the error(s) occurred while attempting.
     */
    void reportError(String failedAction) {
        System.out.printf("Failed to %s input due to the following reason(s):%n", failedAction);

        for (String error : expressionErrorMessages) {
            if (error.equals("ignored")) {
                continue;
            }

            System.out.printf(" -> %s%n", error);
        }
    }

    /**
     * Evaluates an interpreted (grouped by numbers and commands) expression.
     *
     * @param expression the expression to solve.
     *
     * @return the answer to the expression (provided no errors arise while attempting to solve).
     */
    Result evaluate(List<String> expression) {
        // Locate and solve the contents of the rightmost opening bracket until no brackets are remaining, then solve
        //  anything that remains. Applies BEDMAS.
        while (MathReader.brackets.keySet().stream().map(String::valueOf).anyMatch(expression::contains)) {
            int openBracketIndex = -1;
            for (AtomicInteger i = new AtomicInteger(); i.get() < expression.size(); i.addAndGet(1)) {
                if (MathReader.brackets.keySet()
                        .stream()
                        .map(String::valueOf)
                        .anyMatch(b -> expression.get(i.get()).equals(b))) {
                    openBracketIndex = i.get();
                }
            }

            int closeBracketIndex = -1;
            for (AtomicInteger i = new AtomicInteger(openBracketIndex + 1); i.get() < expression.size(); i.addAndGet(1)) {
                // Note that due to prior checking of the input we can assume the brackets match.
                if (MathReader.brackets.values()
                        .stream()
                        .map(String::valueOf)
                        .anyMatch(b -> expression.get(i.get()).equals(b))) {
                    closeBracketIndex = i.get();
                    break;
                }
            }

            Result result = evaluate(expression.subList(openBracketIndex + 1, closeBracketIndex));

            if (!result.successful()) {
                return new UnsuccessfulResult("ignored");
            }

            expression.subList(openBracketIndex, openBracketIndex + 3).clear();
            expression.add(openBracketIndex, String.valueOf(result.result()));
        }

        // If no brackets are present, evaluate the expression using (B)EDMAS.

        Map<OperationOrder, List<Integer>> operationsToCompute = Map.ofEntries(
                entry(OperationOrder.BracketsFunctions, new ArrayList<>()),
                entry(OperationOrder.Exponents, new ArrayList<>()),
                entry(OperationOrder.DivisionMultiplication, new ArrayList<>()),
                entry(OperationOrder.AdditionSubtraction, new ArrayList<>())
        );

        for (int i = 0; i < expression.size(); i++) {
            if (CMath.operations.containsKey(expression.get(i))) {
                operationsToCompute.get(CMath.operations.get(expression.get(i))).add(i);
            }
        }

        // Compute all the functions from rightmost to leftmost.

        while (!operationsToCompute.get(OperationOrder.BracketsFunctions).isEmpty()) {
            int index = operationsToCompute.get(OperationOrder.BracketsFunctions).removeLast();

            if (index == expression.size() - 1) {
                expressionErrorMessages.add(String.format(
                        "No arguments supplied for %s function",
                        expression.get(index)
                ));

                break;
            }

            // TODO: Add support for functions with multiple arguments.
            List<Double> arguments;

            try {
                arguments = List.of(Double.parseDouble(expression.get(index + 1)));
            } catch (NumberFormatException ignored) {
                expressionErrorMessages.add(String.format("%s is not a number...", expression.get(index + 1)));
                break;
            }

            Result result = switch (expression.get(index)) {
                case "sqrt" -> CMath.calculateRoot(arguments);
                case "ln" -> CMath.calculateLogarithm(arguments);
                case "sin" -> CMath.calculateSin(arguments);
                case "cos" -> CMath.calculateCos(arguments);
                case "tan" -> CMath.calculateTan(arguments);
                case "csc" -> CMath.calculateCsc(arguments);
                case "sec" -> CMath.calculateSec(arguments);
                case "cot" -> CMath.calculateCot(arguments);
                default -> new UnsuccessfulResult("Please contact the dev, this shouldn't have happened");
            };

            if (!result.successful()) {
                expressionErrorMessages.add(result.errorMessage()
                        .orElse("Please contact the dev, this shouldn't have happened"));
                break;
            }

            expression.subList(index, index + 2).clear();
            expression.add(index, String.valueOf(result.result()));

            shuntOperationIndices(operationsToCompute, index, 1);
        }

        if (!expressionErrorMessages.isEmpty()) {
            return new UnsuccessfulResult("ignored");
        }

        computeBinaryOperations(operationsToCompute, OperationOrder.Exponents, expression);

        if (!expressionErrorMessages.isEmpty()) {
            return new UnsuccessfulResult("ignored");
        }

        computeBinaryOperations(operationsToCompute, OperationOrder.DivisionMultiplication, expression);

        if (!expressionErrorMessages.isEmpty()) {
            return new UnsuccessfulResult("ignored");
        }

        computeBinaryOperations(operationsToCompute, OperationOrder.AdditionSubtraction, expression);

        if (!expressionErrorMessages.isEmpty()) {
            return new UnsuccessfulResult("ignored");
        }

        if (expression.size() != 1) {
            return new UnsuccessfulResult("No more operators to apply to the remaining numbers");
        }

        return new SuccessfulResult(Double.parseDouble(expression.getFirst()));
    }

    /**
     * Calculates all binary operations found according to (B)EDMAS, and working from left to right.
     *
     * @param allOperationIndices the indices of all binary operations sorted by the order they need to be solved.
     * @param operationTypesToCompute the types of operation to compute with this particular use of the function.
     * @param partiallySolvedExpression the partially solved expression.
     */
    void computeBinaryOperations(
            Map<OperationOrder, List<Integer>> allOperationIndices,
            OperationOrder operationTypesToCompute,
            List<String> partiallySolvedExpression
    ) {
        while (!allOperationIndices.get(operationTypesToCompute).isEmpty()) {
            int index = allOperationIndices.get(operationTypesToCompute).removeFirst();

            if (index == 0 || index == partiallySolvedExpression.size() - 1) {
                expressionErrorMessages.add(String.format(
                        "Not enough arguments (requires 2) supplied for %s",
                        partiallySolvedExpression.get(index)
                ));

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
                default -> throw new RuntimeException("Please contact the dev; this shouldn't have happened...");
            };

            if (!result.successful()) {
                expressionErrorMessages.add(result.errorMessage().orElseThrow());
                break;
            }

            partiallySolvedExpression.subList(index - 1, index + 2).clear();
            partiallySolvedExpression.add(index - 1, String.valueOf(result.result().orElseThrow()));
            shuntOperationIndices(allOperationIndices, index, 2);
        }
    }

    /**
     * Shunts known operation indices by a certain factor. Useful after just computing an operation that combines, say,
     *  5 + 6 (3 entries in the expression list) into 11 (1 entry in the expression list) thus shunting all known
     *  operation indices beyond the + operator down by 2.
     *
     * @param operationsToComplete the known indices of operators in the expression not yet computed.
     * @param completedOperationIndex the index of the operator just computed.
     * @param shuntBy the factor to shunt all known operation indices by.
     */
    void shuntOperationIndices(
            Map<OperationOrder, List<Integer>> operationsToComplete,
            int completedOperationIndex,
            int shuntBy
    ) {
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
     * Obtains a string from the user representing the expression they would like solved, then checks and formats it.
     *
     * @return a boolean indicating whether the read was successful (i.e. had no errors).
     */
    boolean readExpression() {
        System.out.print("Expression: ");

        currentExpression = reader.nextLine()
                .trim()
                .chars()
                .mapToObj(c -> (char)c)
                .filter(c -> c != ' ')
                .map(String::valueOf)
                .collect(Collectors.joining());

        if (nonCalculatorInputs.contains(currentExpression)) {
            return true;
        }

        if (currentExpression.isBlank()) {
            expressionErrorMessages.add("Try entering something...");
        }

        if (!MathReader.bracketsMatching(currentExpression)) {
            expressionErrorMessages.add("Brackets do not match");
        }

        if (MathReader.containsEmptyBrackets(currentExpression)) {
            expressionErrorMessages.add("Expression contains empty brackets");
        }

        return expressionErrorMessages.isEmpty();
    }

    /**
     * Entry point for this program.
     *
     * @param args command line arguments (ignored).
     */
    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        calculator.initialise();
    }
}
