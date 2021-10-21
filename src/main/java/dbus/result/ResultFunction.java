package dbus.result;

import dbus.result.void_.VoidResult;
import dbus.result.void_.VoidResultFunction;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * function extension that allows to provide Result specific composition methods
 *
 * @param <T> The type of the input of the function
 * @param <S> The type of the return type success of the function
 * @param <F> The type of the return type failure of the function
 */
@FunctionalInterface
public interface ResultFunction<T, S, F> extends Function<T, Result<S, F>> {


    /**
     * conversion function to view a Result returning function as a {@link ResultFunction}
     *
     * @param f   the result returning function
     * @param <T> the result returning function entry type
     * @param <S> the returned success type
     * @param <F> the returned failure type
     * @return a {@link ResultFunction} corresponding to the provided result returning function
     * @throws NullPointerException when provided function is null
     */
    static <T, S, F> ResultFunction<T, S, F> asResultFunction(Function<? super T, ? extends Result<? extends S, ? extends F>> f) {
        requireNonNull(f);
        return t -> Result.narrow(f.apply(t));
    }

    /**
     * compose the current {@link ResultFunction} with a success mapping function
     * <p>
     * It applies the provided mapper after this.
     *
     * @param mapper the mapper to apply after this
     * @param <R>    the new success return type
     * @return a {@link ResultFunction} composing this and the mapper
     * @throws NullPointerException when provided mapper is null
     * @see Result#map(Function)
     */
    default <R> ResultFunction<T, R, F> map(Function<? super S, ? extends R> mapper) {
        requireNonNull(mapper);
        return t -> this.apply(t).map(mapper);
    }

    /**
     * bridge function from {@link ResultFunction} to {@link VoidResultFunction}.
     * <p>
     * It applies the provided consumer after the current {@link ResultFunction}.
     *
     * @param consumer the consumer to apply after this
     * @return a {@link VoidResultFunction} composing this and the provided consumer
     * @throws NullPointerException when provided consumer is null
     * @see Result#map(Consumer)
     */
    default VoidResultFunction<T, F> map(Consumer<? super S> consumer) {
        requireNonNull(consumer);
        return t -> this.apply(t).map(consumer);
    }

    /**
     * Result monad bind function applied to a function
     * <p>
     * compose the provided bound function to the current result function.
     *
     * @param bound the function to compose current function with
     * @param <R>   the new success type
     * @return  a result function composing this with the bound function.
     * @throws NullPointerException if provided bound parameter is null
     */
    default <R> ResultFunction<T, R, F> flatMap(Function<? super S, ? extends Result<? extends R, ? extends F>> bound) {
        requireNonNull(bound);
        return t -> this.apply(t).flatMap(bound);
    }

    /**
     * Result monad bind function but with a function that does not need the present success state.
     * <p>
     * compose the provided bound supplier to the current function.
     *
     * @param bound the supplier to compose current function with
     * @param <R>   the new success type
     * @return  a result function composing this with the bound supplier.
     * @throws NullPointerException if provided bound parameter is null
     */
    default <R> ResultFunction<T, R, F> flatMap(Supplier<? extends Result<? extends R, ? extends F>> bound) {
        requireNonNull(bound);
        return t -> this.apply(t).flatMap(bound);
    }

    /**
     * Result monad bind bridge function with a function that returns a {@link VoidResult}.
     * <p>
     * compose the provided bound function to the current function.
     *
     * @param bound the function to compose current function with
     * @return  a void result returning function composing this with the bound function.
     * @throws NullPointerException if provided bound parameter is null
     */
    default VoidResultFunction<T, F> flatMapToVoid(Function<? super S, ? extends VoidResult<? extends F>> bound) {
        requireNonNull(bound);
        return t -> this.apply(t).flatMapToVoid(bound);
    }

    /**
     * Result monad bind bridge function with a supplier that returns a {@link VoidResult}.
     * <p>
     * compose the provided bound supplier to the current function.
     *
     * @param bound the supplier to compose current function with
     * @return  a void result returning function composing this with the bound supplier.
     * @throws NullPointerException if provided bound parameter is null
     */
    default VoidResultFunction<T, F> flatMapToVoid(Supplier<? extends VoidResult<? extends F>> bound) {
        requireNonNull(bound);
        return t -> this.apply(t).flatMapToVoid(bound);
    }

    /**
     * compose current function with a recovering function, transforming any failure into an instance of the success
     * type.
     *
     * @param recoveringFunction recovering function to compose with current function with.
     * @return a function composing this and the recovering function
     * @throws NullPointerException if provided recoveringFunction parameter is null
     */
    default Function<T, S> thenRecover(Function<? super F, ? extends S> recoveringFunction) {
        requireNonNull(recoveringFunction);
        return t -> this.apply(t).recover(recoveringFunction);
    }

    /**
     * compose current function with a recovering supplier, providing an instance of the success type in case of
     * failure.
     *
     * @param recoveringSupplier recovering supplier to compose with current function with.
     * @return a function composing this and the recovering supplier
     * @throws NullPointerException if provided recoveringFunction parameter is null
     */
    default Function<T, S> thenRecover(Supplier<? extends S> recoveringSupplier) {
        requireNonNull(recoveringSupplier);
        return t -> this.apply(t).recover(recoveringSupplier);
    }

}
