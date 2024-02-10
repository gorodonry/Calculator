import java.util.*;
import java.util.Arrays.*;

/**
 * Contains various methods for interpreting an input string as a mathematical expression.
 */
public abstract class MathReader {
    public static String[] BRACKETS = { "(", ")", "[", "]" };

    /**
     * Determines whether an expression contains empty brackets i.e. contains "( )".
     * @param input the expression to check for empty brackets.
     * @return true if the expression contains empty brackets.
     */
    public static boolean containsEmptyBrackets(String input) {
        Queue<Character> remainingChars = new ArrayDeque<>(List.of(input.chars()
                .mapToObj(c -> (char) c)
                .toArray(Character[]::new)));
        System.out.println(remainingChars);
        while (!remainingChars.isEmpty()) {
            if (remainingChars.peek() == '(' || remainingChars.peek() == '[') {
                remainingChars.poll();
                if (!remainingChars.isEmpty()) {
                    if (remainingChars.peek() == ')' || remainingChars.peek() == ']') {
                        return true;
                    }
                }
            }

            remainingChars.poll();
        }

        return false;
    }

    /**
     * Checks that all the brackets in a string are matching. Note: only checks for '(..)' and '[..]'.
     * @param input the string to check.
     * @return true if all brackets are matching.
     */
    public static boolean bracketsMatching(String input) {
        Stack<Character> brackets = new Stack<>();
        Queue<Character> remainingChars = new ArrayDeque<>(List.of(input.chars()
                .mapToObj(c -> (char) c)
                .toArray(Character[]::new)));
        while (!remainingChars.isEmpty()) {
            switch (remainingChars.poll()) {
                case '(' -> brackets.push('(');
                case '[' -> brackets.push('[');
                case ')' -> {
                    if (brackets.isEmpty())
                        return false;

                    if (brackets.pop() != '(')
                        return false;
                }
                case ']' -> {
                    if (brackets.isEmpty())
                        return false;

                    if (brackets.pop() != '[')
                        return false;
                }
                default -> {}
            }
        }

        return brackets.isEmpty();
    }

    /**
     * Interprets a string as a mathematical expression by grouping components together.
     * @param input the string to interpret as a mathematical expression as a list e.g.
     *  -> ["s", "i", "n", "(", "5.32", "+", "67", ")", "/", "l", "o", "g", "-2"].
     * @param errorLog a list for recording any errors encountered while attempting to interpret the string.
     * @return a list of components representing the expression that was interpreted, or an empty optional if errors
     *  were encountered. From the above example, this method would return
     *  -> ["sin", "(", "5.32", "+", "67", ")", "/", "log", "-2"]
     */
    public static Optional<List<String>> readExpression(List<String> input, List<String> errorLog) {
        List<String> currentWorkingSection = new ArrayList<>();
        List<String> formattedExpression = new ArrayList<>();

        boolean currentSectionIsNumber = true;

        for (String component : input) {

            if (component.equals(".")) {
                currentWorkingSection.add(component);

                continue;
            }

            try {
                Integer.parseInt(component);

                if (!currentWorkingSection.isEmpty() && !currentSectionIsNumber) {
                    appendCommand(formattedExpression, currentWorkingSection, errorLog);
                    currentWorkingSection.clear();
                }

                currentWorkingSection.add(component);
                currentSectionIsNumber = true;
            } catch (NumberFormatException ignored) {
                if (!currentWorkingSection.isEmpty() && currentSectionIsNumber) {
                    appendNumber(formattedExpression,
                            Double.parseDouble(String.join("", currentWorkingSection)));
                    currentWorkingSection.clear();
                }

                currentWorkingSection.add(component);
                currentSectionIsNumber = false;
            }
        }

        if (!currentWorkingSection.isEmpty()) {
            if (currentSectionIsNumber) {
                appendNumber(formattedExpression,
                        Double.parseDouble(String.join("", currentWorkingSection)));
            } else {
                appendCommand(formattedExpression, currentWorkingSection, errorLog);
            }
        }

        if (!errorLog.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(formattedExpression);
    }

    /**
     * Appends a number to the expression, taking into account the possibility of a minus sign preceding the number.
     * @param partiallyFormattedExpression the formatted expression so far, to append the number to.
     * @param number the number to append.
     */
    private static void appendNumber(List<String> partiallyFormattedExpression, double number) {
        int numberOfSections = partiallyFormattedExpression.size();

        // Deal with negative numbers. Note that there is no conversion to a negative number when a minus operation
        // occurs; i.e. when a minus is surrounded by bracket(s) and/or number(s).
        if (numberOfSections >= 2) {
            if (partiallyFormattedExpression.get(numberOfSections - 1).equals("-") &&
                    CMath.OPERATION_ORDERS.containsKey(partiallyFormattedExpression.get(numberOfSections - 2))) {
                number *= -1;
                partiallyFormattedExpression.remove(numberOfSections - 1);
            }
        } else if (numberOfSections == 1) {
            if (partiallyFormattedExpression.get(0).equals("-")) {
                number *= -1;
                partiallyFormattedExpression.clear();
            }
        }

        // Check for adjacent numbers, add '*' if this occurs.
        if (!partiallyFormattedExpression.isEmpty()) {
            try {
                Double.parseDouble(partiallyFormattedExpression.get(partiallyFormattedExpression.size() - 1));
                partiallyFormattedExpression.add("*");
            } catch (NumberFormatException ignored) {}
        }

        // Add the number (at long last)!
        partiallyFormattedExpression.add(String.valueOf(number));
    }

    /**
     * Appends a command/series of commands to the expression. Also recognises pi and e as 'commands', and substitutes
     *  them for the numbers they represent.
     * @param partiallyFormattedExpression the formatted expression so far, to append the command(s) to.
     * @param input the command(s) to append.
     * @param errorLog a list for recording any errors encountered while attempting to interpret the command(s).
     */
    private static void appendCommand(List<String> partiallyFormattedExpression, List<String> input, List<String> errorLog) {
        List<String> command = new ArrayList<>();

        for (String component : input) {
            // Check for e.
            if (component.equals("e") && command.isEmpty()) {
                appendNumber(partiallyFormattedExpression, Math.E);

                continue;
            }

            // Check for brackets.
            if (Arrays.asList(BRACKETS).contains(component)) {
                if (!command.isEmpty()) {
                    errorLog.add(String.format("Unrecognised command: %s", String.join("", command)));
                    command.clear();
                }

                partiallyFormattedExpression.add(component);

                continue;
            }

            command.add(component);

            // Check for pi.
            if (String.join("", command).equals("pi")) {
                appendNumber(partiallyFormattedExpression, Math.PI);
                command.clear();

                continue;
            }

            if (CMath.OPERATION_ORDERS.containsKey(String.join("", command))) {
                partiallyFormattedExpression.add(String.join("", command));
                command.clear();
            }
        }

        if (!command.isEmpty()) {
            errorLog.add(String.format("Unrecognised command: %s", String.join("", command)));
        }
    }
}
