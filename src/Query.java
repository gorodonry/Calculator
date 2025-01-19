/**
 * Represents a query made by the user.
 *
 * @param expression the mathematical expression entered by the user.
 * @param answer the answer to the expression.
 */
public record Query(String expression, double answer) {}
