import java.util.Optional;

/**
 * Stores the result of a successful calculation.
 */
public class SuccessfulResult implements Result {
    double result;

    /**
     * Instantiates a new successful result.
     * @param result the result of the successful calculation.
     */
    public SuccessfulResult(double result) {
        this.result = result;
    }

    /**
     * Gets the result stored.
     * @return the result stored.
     */
    public Optional<Double> result() {
        return Optional.of(result);
    }

    /**
     * Gets a boolean indicating whether the calculation was successful.
     * @return true.
     */
    public boolean successful() {
        return true;
    }

    /**
     * Gets a string explaining why the calculation failed.
     * @return null.
     */
    public Optional<String> errorMessage() {
        return Optional.empty();
    }
}
