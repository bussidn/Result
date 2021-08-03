package dbus;

import lombok.EqualsAndHashCode;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
final class Success<S, F> implements Result<S, F> {

    private final S value;

    private Success(S value) {
        this.value = requireNonNull(value);
    }

    public static <S, F> Success<S, F> success(S value) {
        return new Success<>(value);
    }

    @Override
    public <R> R match(
            Function<? super Success<S, F>, ? extends R> success,
            Function<? super Failure<S, F>, ? extends R> failure
    ) {
        return success.apply(this);
    }
}
