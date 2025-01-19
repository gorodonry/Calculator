import java.util.Optional;

/**
 * Stores the result of a successful calculation.
 *
 * @param value the result of the calculation.
 */
public record SuccessfulResult(double value) implements Result {
    @Override
    public Optional<Double> result() {
        return Optional.of(value);
    }

    @Override
    public boolean successful() {
        return true;
    }
}
