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

    @Override
    public <R> Result<R, F> map(Function<? super S, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        return new Success<>(mapper.apply(this.value));
    }

    @Override
    public VoidResult<F> map(Consumer<? super S> consumer) {
        consumer.accept(value);
        return VoidResult.success();
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
}
