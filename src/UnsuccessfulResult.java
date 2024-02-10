/**
 * Stores the error message of an unsuccessful calculation.
 */
public record UnsuccessfulResult(String errorMessage) implements Result {
    /**
     * Gets the result of the calculation.
     * @return Double.NaN.
     */
    public double result() {
        return Double.NaN;
    }

    /**
     * Gets a boolean indicating whether the calculation was successful.
     * @return false.
     */
    public boolean successful() {
        return false;
    }
}
