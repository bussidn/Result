package dbus.result.void_;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 */
public interface VoidResult<F> {

    static <F> Success<F> success() {
        return Success.success();
    }

    static <F> dbus.result.void_.Failure<F> failure(F value) {
        return Failure.failure(value);
    }

    <R> R match(
            Supplier<? extends R> success,
            Function<? super Failure<F>, ? extends R> failure
    );
}
