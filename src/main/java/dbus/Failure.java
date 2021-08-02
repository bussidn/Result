package dbus;

import lombok.EqualsAndHashCode;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
final class Failure<S, F> implements Result<S, F> {

    private final F value;

    public Failure(F value) {
        this.value = requireNonNull(value);
    }

    @Override
    public <R> R match(
            Function<? super Success<S, F>, ? extends R> success,
            Function<? super Failure<S, F>, ? extends R> failure
    ) {
        return failure.apply(this);
    }
}
