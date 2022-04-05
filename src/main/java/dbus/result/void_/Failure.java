package dbus.result.void_;

import dbus.result.Result;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static dbus.result.void_.VoidResult.narrow;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
final public class Failure<F> implements VoidResult<F> {

    private final F value;

    public Failure(F value) {
        this.value = requireNonNull(value);
    }

    public static <F> Failure<F> failure(F value) {
        return new Failure<>(value);
    }

    @Override
    public <R> R match(Supplier<? extends R> success, Function<? super F, ? extends R> failure) {
        requireNonNull(success);
        return failure.apply(value);
    }

    @Override
    public VoidResult<F> map(Runnable runnable) {
        requireNonNull(runnable);
        return this;
    }

    @Override
    public <S> Result<S, F> map(Supplier<? extends S> supplier) {
        requireNonNull(supplier);
        return Result.failure(value);
    }

    @Override
    public <G> VoidResult<G> mapFailure(Function<? super F, ? extends G> mapper) {
        return failure(requireNonNull(mapper).apply(value));
    }

    @Override
    public <G> VoidResult<G> mapFailure(Supplier<? extends G> mapper) {
        return failure(requireNonNull(mapper).get());
    }

    @Override
    public void mapFailure(Consumer<? super F> consumer) {
        requireNonNull(consumer).accept(value);
    }

    @Override
    public VoidResult<F> flatMap(Supplier<? extends VoidResult<? extends F>> bound) {
        requireNonNull(bound);
        return this;
    }

    @Override
    public <R> Result<R, F> flatMapToResult(Supplier<? extends Result<? extends R, ? extends F>> bound) {
        requireNonNull(bound);
        return Result.failure(value);
    }

    @Override
    public VoidResult<F> tryRecovering(Function<? super F, ? extends VoidResult<? extends F>> recoveringFunction) {
        return narrow(recoveringFunction.apply(value));
    }

    @Override
    public VoidResult<F> tryRecovering(Supplier<? extends VoidResult<? extends F>> recoveringSupplier) {
        return narrow(recoveringSupplier.get());
    }

}
