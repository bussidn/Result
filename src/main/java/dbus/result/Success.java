package dbus.result;

import dbus.result.void_.VoidResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static dbus.result.Result.narrow;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
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
            Function<? super S, ? extends R> success,
            Function<? super F, ? extends R> failure
    ) {
        requireNonNull(failure);
        return success.apply(value);
    }

    private <ANY> Result<S, ANY> cast() {
        //noinspection unchecked
        return (Result<S, ANY>) this;
    }

    @Override
    public <R> Result<R, F> map(Function<? super S, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        return new Success<>(mapper.apply(this.value));
    }

    @Override
    public <R> Result<R, F> map(Supplier<? extends R> mapper) {
        return success(mapper.get());
    }

    @Override
    public VoidResult<F> map(Consumer<? super S> consumer) {
        consumer.accept(value);
        return VoidResult.success();
    }

    @Override
    public <G> Result<S, G> mapFailure(Function<? super F, ? extends G> mapper) {
        requireNonNull(mapper);
        return this.cast();
    }

    @Override
    public <G> Result<S, G> mapFailure(Supplier<? extends G> mapper) {
        requireNonNull(mapper);
        return this.cast();
    }

    @Override
    public <R> Result<R, F> flatMap(Function<? super S, ? extends Result<? extends R, ? extends F>> bound) {
        requireNonNull(bound);
        return narrow(bound.apply(value));
    }

    @Override
    public <R> Result<R, F> flatMap(Supplier<? extends Result<? extends R, ? extends F>> bound) {
        requireNonNull(bound);
        return narrow(bound.get());
    }

    @Override
    public VoidResult<F> flatMapToVoid(Function<? super S, ? extends VoidResult<? extends F>> bound) {
        return VoidResult.narrow(bound.apply(value));
    }

    @Override
    public VoidResult<F> flatMapToVoid(Supplier<? extends VoidResult<? extends F>> bound) {
        return VoidResult.narrow(bound.get());
    }

    @Override
    public S recover(Function<? super F, ? extends S> recoveringFunction) {
        requireNonNull(recoveringFunction);
        return value;
    }

    @Override
    public S recover(Supplier<? extends S> recoveringSupplier) {
        requireNonNull(recoveringSupplier);
        return value;
    }

    @Override
    public Result<S, F> tryRecovering(Function<? super F, ? extends Result<? extends S, ? extends F>> recoveringFunction) {
        requireNonNull(recoveringFunction);
        return this;
    }

    @Override
    public Result<S, F> tryRecovering(Supplier<? extends Result<? extends S, ? extends F>> recoveringSupplier) {
        requireNonNull(recoveringSupplier);
        return this;
    }
}
