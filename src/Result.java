/**
 * Stores information about the result of a calculation.
 */
public interface Result {
    /**
     * Gets the result stored.
     * @return the result stored.
     */
    double result();

    /**
     * Gets a boolean indicating whether the calculation that obtained this result was performed successfully.
     * @return a boolean indicating whether the calculation that obtained this result was performed successfully.
     */
    boolean successful();

    /**
     * Gets an error message explaining why the calculation failed (if applicable).
     * @return an error message explaining why the calculation failed, or null if it was successful.
     */
    String errorMessage();
}
