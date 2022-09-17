package dbus.result.void_;

import dbus.result.Result;
import dbus.result.ResultFunction;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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
     * Generates a void result depending on the value of the provided boolean.
     * If the provided boolean is true, the result will be a success, else, it will
     * return a failure containing the provided failure.
     *
     * @param predicate predicate that serves as a test
     * @param failure the failure to wrap in case provide {@code bool} is false
     * @return a void result
     * @param <F> the failure type
     * @throws NullPointerException when provided failure is null
     */
    static <T, F> VoidResultFunction<T, F> successIf(Predicate<T> predicate, F failure) {
        requireNonNull(predicate);
        requireNonNull(failure);
        return t -> VoidResult.successIf(predicate.test(t), failure);
    }

    /**
     * Generates a void result depending on the value of the provided boolean.
     * If the provided boolean is true, the result will be a success, else, it will
     * return a failure containing the failure returned by the provided supplier.
     *
     * @param predicate predicate that serves as a test
     * @param failure a supplier of the failure to wrap in case provide {@code bool} is false
     * @return a void result
     * @param <F> the failure type
     * @throws NullPointerException when provided failure is null
     */
    static <T, F> VoidResultFunction<T, F> successIf(Predicate<T> predicate, Supplier<? extends F> failure) {
        requireNonNull(predicate);
        requireNonNull(failure);
        return t -> VoidResult.successIf(predicate.test(t), failure);
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
     * compose the failure side of the current {@link VoidResultFunction} with a failure mapping function.
     * <p>
     * It applies the provided mapper after this.
     *
     * @param mapper the mapper to apply after this
     * @param <R>    the new failure return type
     * @return a {@link VoidResultFunction} composing this and the mapper
     * @throws NullPointerException when provided mapper is null
     * @see VoidResult#mapFailure(Function)
     */
    default <R> VoidResultFunction<T, R> mapFailure(Function<? super F, ? extends R> mapper) {
        requireNonNull(mapper);
        return t -> this.apply(t).mapFailure(mapper);
    }

    /**
     * compose the failure side of the current {@link VoidResultFunction} with a supplier.
     * <p>
     * It applies the provided supplier after this.
     *
     * @param supplier the mapper to apply after this
     * @param <R>      the new failure return type
     * @return a {@link VoidResultFunction} composing this and the mapper
     * @throws NullPointerException when provided supplier is null
     * @see VoidResult#mapFailure(Supplier)
     */
    default <R> VoidResultFunction<T, R> mapFailure(Supplier<? extends R> supplier) {
        requireNonNull(supplier);
        return t -> this.apply(t).mapFailure(supplier);
    }

    /**
     * compose the failure side of the current {@link VoidResultFunction} with a consumer.
     * <p>
     * It applies the provided consumer after the current {@link VoidResultFunction}.
     *
     * @param consumer the consumer to apply after this
     * @return a {@link VoidResultFunction} composing this and the provided consumer
     * @throws NullPointerException when provided consumer is null
     * @see VoidResult#mapFailure(Consumer)
     */
    default Consumer<T> mapFailure(Consumer<? super F> consumer) {
        requireNonNull(consumer);
        return t -> this.apply(t).mapFailure(consumer);
    }

    /**
     * compose the current {@link VoidResultFunction} with a void result supplier
     * <p>
     * It applies the provided supplier after this if the result is a success.
     * Returns the current failure otherwise.
     * <p>
     *
     * @param bound the runnable to apply after this
     * @return a {@link VoidResultFunction} composing this and the provided bound supplier
     * @throws NullPointerException when provided mapper is null
     * @see VoidResult#map(Runnable)
     */
    default VoidResultFunction<T, F> flatMap(Supplier<? extends VoidResult<F>> bound) {
        requireNonNull(bound);
        return t -> this.apply(t).flatMap(bound);
    }

    /**
     * compose the current {@link VoidResultFunction} with a result supplier
     * <p>
     * It applies the provided supplier after this if the result is a success.
     * Returns the current failure otherwise.
     *
     * @param bound the supplier to compose current result with
     * @param <S>   the new success type
     * @return a {@link ResultFunction} composing this and the provided bound supplier
     * @throws NullPointerException if provided bound parameter is null
     */
    default <S> ResultFunction<T, S, F> flatMapToResult(Supplier<? extends Result<? extends S, ? extends F>> bound) {
        requireNonNull(bound);
        return t -> this.apply(t).flatMapToResult(bound);
    }

    /**
     * compose the current {@link VoidResultFunction} with a recovering function that may also fail.
     *
     * @param recoveringFunction function to compose with the current function
     * @return a function composing this and the recovering function
     * @throws NullPointerException if provided recoveringFunction parameter is null
     */
    default VoidResultFunction<T, F> thenTryRecovering(Function<? super F, ? extends VoidResult<? extends F>> recoveringFunction) {
        requireNonNull(recoveringFunction);
        return t -> this.apply(t).tryRecovering(recoveringFunction);
    }

    /**
     * compose the current {@link VoidResultFunction} with a recovering supplier that may also fail.
     *
     * @param recoveringSupplier supplier to compose with the current function
     * @return a function composing this and the recovering supplier
     * @throws NullPointerException if provided recoveringSupplier parameter is null
     */
    default VoidResultFunction<T, F> thenTryRecovering(Supplier<? extends VoidResult<? extends F>> recoveringSupplier) {
        requireNonNull(recoveringSupplier);
        return t -> this.apply(t).tryRecovering(recoveringSupplier);
    }
}
