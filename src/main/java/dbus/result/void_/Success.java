package dbus.result.void_;

import dbus.result.Result;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
final public class Success<F> implements VoidResult<F> {

    private static final Success<?> SUCCESS = new Success<>();

    private Success() {
    }

    public static <F> Success<F> success() {
        return SUCCESS.cast();
    }

    private <G> Success<G> cast() {
        //noinspection unchecked
        return (Success<G>) this;
    }

    @Override
    public <R> R match(Supplier<? extends R> success, Function<? super F, ? extends R> failure) {
        requireNonNull(failure);
        return success.get();
    }

    @Override
    public VoidResult<F> map(Runnable runnable) {
        requireNonNull(runnable);
        runnable.run();
        return this;
    }

    @Override
    public <S> Result<S, F> map(Supplier<? extends S> supplier) {
        return Result.success(supplier.get());
    }

    @Override
    public <R> Result<R, F> flatMap(Supplier<? extends Result<? extends R, ? extends F>> bound) {
        requireNonNull(bound);
        return Result.narrow(bound.get());
    }

}
