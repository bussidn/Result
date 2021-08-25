package dbus.result;

import dbus.result.void_.VoidResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
final class Failure<S, F> implements Result<S, F> {

    private final F value;

    private Failure(F value) {
        this.value = requireNonNull(value);
    }

    public static <S, F> Failure<S, F> failure(F value) {
        return new Failure<>(value);
    }

    @Override
    public <R> R match(
            Function<? super S, ? extends R> success,
            Function<? super F, ? extends R> failure
    ) {
        requireNonNull(success);
        return failure.apply(value);
    }

    private <ANY> Result<ANY, F> cast() {
        //noinspection unchecked
        return (Result<ANY, F>) this;
    }

    @Override
    public <R> Result<R, F> map(Function<? super S, ? extends R> mapper) {
        requireNonNull(mapper);
        return this.cast();
    }

    @Override
    public VoidResult<F> map(Consumer<? super S> consumer) {
        requireNonNull(consumer);
        return VoidResult.failure(value);
    }

    @Override
    public <R> Result<R, F> flatMap(Function<? super S, ? extends Result<? extends R, ? extends F>> bound) {
        requireNonNull(bound);
        return this.cast();
    }
}
