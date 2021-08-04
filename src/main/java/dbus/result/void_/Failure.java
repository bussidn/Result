package dbus.result.void_;

import lombok.EqualsAndHashCode;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
final public class Failure<F> implements VoidResult<F> {

    private final F value;

    public Failure(F value) {
        this.value = requireNonNull(value);
    }

    public static <F> Failure<F> failure(F value) {
        return new Failure<>(value);
    }

    @Override
    public <R> R match(Supplier<? extends R> success, Function<? super Failure<F>, ? extends R> failure) {
        requireNonNull(success);
        return failure.apply(this);
    }

}
