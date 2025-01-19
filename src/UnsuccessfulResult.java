import java.util.Optional;

/**
 * Stores the error message of an unsuccessful calculation.
 *
 * @param reason the reason the calculation failed.
 */
public record UnsuccessfulResult(String reason) implements Result {
    @Override
    public boolean successful() {
        return false;
    }

    @Override
    public Optional<String> errorMessage() {
        return Optional.of(reason);
    }
}
