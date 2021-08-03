package dbus;

import java.util.function.Function;

/**
 * Class representing either a success or a failure.
 *
 * @param <S> the success type
 * @param <F> the failure type
 */
public interface Result<S, F> {

    static <S, F> Result<S, F> success(S value) {
        return Success.success(value);
    }

    static <S, F> Result<S, F> failure(F value) {
        return Failure.failure(value);
    }

    <R> R match(
            Function<? super Success<S, F>, ? extends R> success,
            Function<? super Failure<S, F>, ? extends R> failure
    );
}
