package dbus.result;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Objects;
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
            Function<? super Success<S, F>, ? extends R> success,
            Function<? super Failure<S, F>, ? extends R> failure
    ) {
        requireNonNull(success);
        return failure.apply(this);
    }

    private <ANY> Result<ANY, F> cast() {
        //noinspection unchecked
        return (Result<ANY, F>) this;
    }

    @Override
    public <R> Result<R, F> map(Function<? super S, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        return this.cast();
    }
}
