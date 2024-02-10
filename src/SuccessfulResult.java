/**
 * Stores the result of a successful calculation.
 */
public record SuccessfulResult(double result) implements Result {
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
    public String errorMessage() {
        return null;
    }
}
