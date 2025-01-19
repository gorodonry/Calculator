import java.util.*;

/**
 * Contains various methods for parsing an input string as a mathematical expression.
 */
public final class MathReader {
    public static final Map<Character, Character> brackets = Map.ofEntries(
            Map.entry('(', ')'),
            Map.entry('[', ']')
    );

    /**
     * Determines whether an expression contains empty brackets i.e. contains "()" or "[]".
     *
     * @param input the expression to check for empty brackets.
     *
     * @return true if the expression contains empty brackets.
     */
    public static boolean containsEmptyBrackets(String input) {
        Queue<Character> remainingChars = new ArrayDeque<>(
                input.chars().mapToObj(c -> (char) c).toList()
        );

        while (remainingChars.size() > 1) {
            if (brackets.containsKey(remainingChars.poll()) &&
                    brackets.containsValue(Objects.requireNonNull(remainingChars.peek()))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks that all the brackets in a string are matching. Note: only checks for '(..)' and '[..]'.
     *
     * @param input the string to check.
     *
     * @return true if all brackets are matching.
     */
    public static boolean bracketsMatching(String input) {
        Stack<Character> unmatchedBrackets = new Stack<>();
        Queue<Character> remainingChars = new ArrayDeque<>(
                input.chars().mapToObj(c -> (char) c).toList()
        );

        while (!remainingChars.isEmpty()) {
            char character = remainingChars.poll();

            if (brackets.containsKey(character)) {
                unmatchedBrackets.push(character);
            }

            if (brackets.containsValue(character) && brackets.get(unmatchedBrackets.pop()) != character) {
                return false;
            }
        }

        return unmatchedBrackets.isEmpty();
    }

    /**
     * Interprets a string as a mathematical expression by grouping components together.
     *
     * @param input the string to interpret as a mathematical expression as a list e.g.
     *              ["s", "i", "n", "(", "5.32", "+", "67", ")", "/", "l", "o", "g", "-2"].
     * @param errorLog a list for recording any errors encountered while attempting to interpret the string.
     * @param constants a map of all named constants. Note user defined constants are upper case letters and are named,
     *                  but may be keyed to empty optionals.
     *
     * @return a list of components representing the expression that was interpreted, or an empty optional if errors
     *         were encountered. From the above example, this method would return
     *         ["sin", "(", "5.32", "+", "67", ")", "/", "log", "-2"]
     */
    public static Optional<List<String>> readExpression(
            List<String> input,
            List<String> errorLog,
            Map<String, Optional<Double>> constants
    ) {
        List<String> currentWorkingSection = new ArrayList<>();
        List<String> formattedExpression = new ArrayList<>();
        ExpressionSection currentSectionType = ExpressionSection.Number;

        for (String component : input) {
            // Decimal points are treated separately, as they fail the integer parse test, but only constitute parts
            //  of numbers.
            if (component.equals(".")) {
                currentWorkingSection.add(component);
                continue;
            }

            // A switch between the try block and the catch block running indicates a switch between a number
            //  component and a command component. This property is used to distinguish parts of the expression
            //  from each other, which are then parsed by sub-methods.
            try {
                Integer.parseInt(component);

                if (!currentWorkingSection.isEmpty() && currentSectionType == ExpressionSection.Command) {
                    appendCommand(formattedExpression, currentWorkingSection, errorLog, constants);
                    currentWorkingSection.clear();
                }

                currentWorkingSection.add(component);
                currentSectionType = ExpressionSection.Number;
            } catch (NumberFormatException ignored) {
                if (!currentWorkingSection.isEmpty() && currentSectionType == ExpressionSection.Number) {
                    appendNumber(
                            formattedExpression,
                            Double.parseDouble(String.join("", currentWorkingSection))
                    );

                    currentWorkingSection.clear();
                }

                currentWorkingSection.add(component);
                currentSectionType = ExpressionSection.Command;
            }
        }

        // There will be a residual component upon exiting the loop, this is parsed here.
        if (!currentWorkingSection.isEmpty()) {
            switch (currentSectionType) {
                case Number -> appendNumber(
                        formattedExpression,
                        Double.parseDouble(String.join("", currentWorkingSection))
                );
                case Command -> appendCommand(
                        formattedExpression,
                        currentWorkingSection,
                        errorLog,
                        constants
                );
            }
        }

        if (!errorLog.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(formattedExpression);
    }

    /**
     * Appends a number to the expression, taking into account the possibility of a minus sign preceding the number.
     *
     * @param partiallyFormattedExpression the formatted expression so far, to append the number to.
     * @param number the number to append.
     */
    private static void appendNumber(List<String> partiallyFormattedExpression, double number) {
        int numberOfSections = partiallyFormattedExpression.size();

        // Account for negative numbers. Note that there is no conversion to a negative number when a minus operation
        // occurs; i.e. when a minus is surrounded by bracket(s) and/or number(s).
        if (numberOfSections >= 2) {
            if (partiallyFormattedExpression.get(numberOfSections - 1).equals("-") &&
                    CMath.operations.containsKey(partiallyFormattedExpression.get(numberOfSections - 2))) {
                number *= -1;
                partiallyFormattedExpression.remove(numberOfSections - 1);
            }
        } else if (numberOfSections == 1) {
            if (partiallyFormattedExpression.getFirst().equals("-")) {
                number *= -1;
                partiallyFormattedExpression.clear();
            }
        }

        // Check for adjacent numbers, insert '*' between them if this occurs.
        if (!partiallyFormattedExpression.isEmpty()) {
            try {
                Double.parseDouble(partiallyFormattedExpression.getLast());
                partiallyFormattedExpression.add("*");
            } catch (NumberFormatException ignored) {}
        }

        // Add the number (at long last)!
        partiallyFormattedExpression.add(String.valueOf(number));
    }

    /**
     * Appends a command/series of commands to the expression. Also recognises pi and e as 'commands', and substitutes
     *  them for the numbers they represent.
     *
     * @param partiallyFormattedExpression the formatted expression so far, to append the command(s) to.
     * @param input the command(s) to append.
     * @param constants a map of all named constants. Note user defined constants are upper case letters and are named,
     *                  but may be keyed to empty optionals.
     * @param errorLog a list for recording any errors encountered while attempting to interpret the command(s).
     */
    private static void appendCommand(
            List<String> partiallyFormattedExpression,
            List<String> input,
            List<String> errorLog,
            Map<String, Optional<Double>> constants
    ) {
        List<String> command = new ArrayList<>();

        for (String component : input) {
            // Check for single letter constants. Note we have to check the command is currently empty because of e,
            //  which can also be found in function names such as secant. User defined constants are upper case letters.
            if (constants.containsKey(component) && command.isEmpty()) {
                constants.get(component).ifPresentOrElse(
                        value -> appendNumber(partiallyFormattedExpression, value),
                        () -> errorLog.add(String.format("Undefined constant: %s", component))
                );
            }

            // Check for brackets. Note that the component is guaranteed to be a string containing a single character.
            if ((brackets.containsKey(component.charAt(0)) || brackets.containsValue(component.charAt(0)))) {
                if (!command.isEmpty()) {
                    errorLog.add(String.format("Unrecognised command: %s", String.join("", command)));
                    command.clear();
                }

                partiallyFormattedExpression.add(component);
                continue;
            }

            command.add(component);

            // Check for multiple letter constants (e.g. pi and ans).
            if (constants.containsKey(String.join("", command)) && command.size() >= 2) {
                if (constants.get(String.join("", command)).isPresent()) {
                    appendNumber(partiallyFormattedExpression, constants.get(String.join("", command)).get());
                } else {
                    errorLog.add(String.format("No calculator history found for %s", String.join("", command)));
                }

                command.clear();
                continue;
            }

            if (CMath.operations.containsKey(String.join("", command))) {
                partiallyFormattedExpression.add(String.join("", command));
                command.clear();
            }
        }

        if (!command.isEmpty()) {
            errorLog.add(String.format("Unrecognised command: %s", String.join("", command)));
        }
    }
}
