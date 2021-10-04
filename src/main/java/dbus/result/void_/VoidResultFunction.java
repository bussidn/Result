package dbus.result.void_;

import dbus.result.ResultFunction;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface VoidResultFunction<T, F> extends Function<T, VoidResult<F>> {

    /**
     * conversion function to view a VoidResult returning function as a {@link VoidResultFunction}
     *
     * @param f   the void result returning function
     * @param <T> the result returning function entry type
     * @param <F> the returned failure type
     * @return a {@link VoidResultFunction} corresponding to the provided result returning function
     * @throws NullPointerException when provided function is null
     */
    static <T, F> VoidResultFunction<T, F> asVoidResultFunction(Function<? super T, ? extends VoidResult<? extends F>> f) {
        Objects.requireNonNull(f);
        return t -> VoidResult.narrow(f.apply(t));
    }

    /**
     * compose the current {@link VoidResultFunction} with a runnable in case of success.
     * <p>
     * It applies the provided runnable after this if the result is a success.
     * <p>
     * behaves similarly as the peek method.
     *
     * @param runnable the runnable to apply after this
     * @return a {@link VoidResultFunction} composing this and the runnable
     * @throws NullPointerException when provided mapper is null
     * @see VoidResult#map(Runnable)
     */
    default VoidResultFunction<T, F> map(Runnable runnable) {
        requireNonNull(runnable);
        return t -> this.apply(t).map(runnable);
    }

    /**
     * bridge function from {@link VoidResultFunction} to {@link ResultFunction}.
     * <p>
     * It executes the provided supplier after the current {@link VoidResultFunction} in case of success.
     *
     * @param supplier the supplier to execute after this
     * @return a {@link VoidResultFunction} composing this and the provided consumer
     * @throws NullPointerException when provided consumer is null
     * @see VoidResult#map(Supplier)
     */
    default <S> ResultFunction<T, S, F> map(Supplier<? extends S> supplier) {
        requireNonNull(supplier);
        return t -> this.apply(t).map(supplier);
    }

    /**
     * compose the current {@link VoidResultFunction} with a void result supplier
     * <p>
     * It applies the provided supplier after this if the result is a success.
     * Returns the current failure otherwise.
     * <p>
     *
     * @param bound the runnable to apply after this
     * @return a {@link VoidResultFunction} composing this and the runnable
     * @throws NullPointerException when provided mapper is null
     * @see VoidResult#map(Runnable)
     */
    default VoidResultFunction<T, F> flatMap(Supplier<? extends VoidResult<F>> bound) {
        requireNonNull(bound);
        return t -> this.apply(t).flatMap(bound);
    }
}
