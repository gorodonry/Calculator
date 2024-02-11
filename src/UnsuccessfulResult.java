import java.util.Optional;

/**
 * Stores the error message of an unsuccessful calculation.
 */
public class UnsuccessfulResult implements Result {
    String errorMessage;

    /**
     * Instantiates a new unsuccessful result.
     * @param errorMessage the reason the calculation was unsuccessful.
     */
    public UnsuccessfulResult(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Gets the result of the calculation.
     * @return an empty optional.
     */
    public Optional<Double> result() {
        return Optional.empty();
    }

    /**
     * Gets a boolean indicating whether the calculation was successful.
     * @return false.
     */
    public boolean successful() {
        return false;
    }

    /**
     * Gets the error message associated with this result.
     * @return an optional containing the stored error message.
     */
    public Optional<String> errorMessage() {
        return Optional.of(errorMessage);
    }
}
