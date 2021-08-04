package dbus.result.void_;

import lombok.EqualsAndHashCode;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
final public class Success<F> implements VoidResult<F> {

    public static <F> Success<F> success() {
        return new Success<>();
    }

    @Override
    public <R> R match(Supplier<? extends R> success, Function<? super Failure<F>, ? extends R> failure) {
        requireNonNull(failure);
        return success.get();
    }

}
