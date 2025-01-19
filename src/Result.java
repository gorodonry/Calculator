import java.util.Optional;

/**
 * Stores the result of a calculation.
 */
public interface Result {
    /**
     * Gets the result of the calculation.
     *
     * @return the result of the calculation.
     */
    default Optional<Double> result() {
        return Optional.empty();
    }

    /**
     * Gets a boolean indicating whether the calculation that obtained this result was performed successfully.
     *
     * @return true if the calculation was successful, false otherwise.
     */
    boolean successful();

    /**
     * Gets an error message explaining why the calculation failed (if applicable).
     *
     * @return an error message explaining why the calculation failed.
     */
    default Optional<String> errorMessage() {
        return Optional.empty();
    }
}
