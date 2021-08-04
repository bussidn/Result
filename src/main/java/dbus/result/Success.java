package dbus.result;

import dbus.result.void_.VoidResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

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
            Function<? super Success<S, F>, ? extends R> success,
            Function<? super Failure<S, F>, ? extends R> failure
    ) {
        requireNonNull(failure);
        return success.apply(this);
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
}
