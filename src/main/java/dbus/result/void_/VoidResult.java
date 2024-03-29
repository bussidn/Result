package dbus.result.void_;

import dbus.result.Result;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Result that does not have a success value
 */
public sealed interface VoidResult<F> permits Success, Failure {

    /**
     * Static factory to create an untyped success
     *
     * @param <F> the failure type.
     * @return a success of type S
     */
    static <F> Success<F> success() {
        return Success.success();
    }

    /**
     * Static factory to create a failure
     *
     * @param value the value of the failure.
     * @param <F>   the failure type.
     * @return a failure of type F
     * @throws NullPointerException when provided value is null
     */
    static <F> dbus.result.void_.Failure<F> failure(F value) {
        return Failure.failure(value);
    }

    /**
     * Narrow the scope of failure type to a supertype of the provided void result.
     *
     * @param voidResult the result that must be narrows
     * @param <F> the desired failure type
     * @return a narrowed result
     * @throws NullPointerException if provided result is null
     */
    static <F> VoidResult<F> narrow(VoidResult<? extends F> voidResult) {
        return voidResult.match(VoidResult::success, VoidResult::failure);
    }

    /**
     * generates a void result depending on the value of the provided boolean.
     * If the provided boolean is true, the result will be a success, else, it will
     * return a failure containing the provided failure.
     *
     * @param bool the boolean that serves as a test
     * @param failure the failure to wrap in case provide {@code bool} is false
     * @return a void result
     * @param <F> the failure type
     */
    static <F> VoidResult<F> successIf(boolean bool, F failure) {
        requireNonNull(failure);
        return bool ? VoidResult.success() : VoidResult.failure(failure);
    }

    /**
     * generates a void result depending on the value of the provided boolean.
     * If the provided boolean is true, the result will be a success, else, it will
     * return a failure containing the provided failure.
     *
     * @param bool the boolean that serves as a test
     * @param failure the failure to wrap in case provide {@code bool} is false
     * @return a void result
     * @param <F> the failure type
     * @throws NullPointerException when provided failure is null
     */
    static <F> VoidResult<F> successIf(boolean bool, Supplier<? extends F> failure) {
        requireNonNull(failure);
        return bool ? VoidResult.success() : VoidResult.failure(failure.get());
    }

    /**
     * Pattern matching emulation on VoidResult subtypes.
     * <p>
     * Indented to be used to provide the two desired behavior in case of success or failure.
     *
     * @param success the desired behavior in case of success.
     * @param failure the desired behavior in cas of failure
     * @param <R>     the new type
     * @return a value of the new type provided by one of the two provided functions
     * @throws NullPointerException if any of its arguments is null
     */
    <R> R match(
            Supplier<? extends R> success,
            Function<? super F, ? extends R> failure
    );

    /**
     * <p>
     * It executes the provided runnable if this is a success.
     * Returns the current failure otherwise.
     *
     * @param runnable the runnable to execute in case of success
     * @return a {@link VoidResult} containing either a success or the current failure
     * @throws NullPointerException when provided supplier is null
     */
    VoidResult<F> map(Runnable runnable);

    /**
     * bridge function for VoidResult functor to Result.
     * <p>
     * It executes the provided supplier if this is a success.
     * Returns the current failure otherwise as a Result.Failure.
     *
     * @param supplier the supplier to execute in case of success
     * @return a Result containing either a success with the supplied value or the current failure
     * @throws NullPointerException when provided supplier is null
     */
    <S> Result<S, F> map(Supplier<? extends S> supplier);

    /**
     * VoidResult functor map function.
     * <p>
     * It applies the provided mapper to the failure value if this is a failure.
     * Returns the current success otherwise.
     *
     * @param mapper the mapper to apply to the failure
     * @param <G>    the new success return type
     * @return a VoidResult containing either the current success or a mapped failure
     * @throws NullPointerException when provided mapper is null
     */
    <G> VoidResult<G> mapFailure(Function<? super F, ? extends G> mapper);

    /**
     * VoidResult functor map function.
     * <p>
     * It executes the provided mapper if this is a failure and keep the provided value as the new failure.
     * Returns the current success otherwise.
     *
     * @param mapper the mapper to apply to the failure
     * @param <G>    the new success return type
     * @return a VoidResult containing either the current success or a supplied failure
     * @throws NullPointerException when provided mapper is null
     */
    <G> VoidResult<G> mapFailure(Supplier<? extends G> mapper);

    /**
     * consumes the current failure if any, discard it otherwise
     * <p>
     * It applies the provided consumer to the failure value if this is a failure.
     * Returns the current success otherwise as an Optional.
     *
     * @param consumer the consumer to apply to the failure
     * @throws NullPointerException when provided consumer is null
     */
    void mapFailure(Consumer<? super F> consumer);

    /**
     * VoidResult monad bind function.
     * <p>
     * compose the provided bound supplier to the current success if any.
     * If current state is a failure, the provided bound function is not called.
     *
     * @param bound the supplier to compose current result with
     * @return  a void result containing either the success if current and bound supplied result are successes,
     *          a F-typed failure otherwise.
     * @throws NullPointerException if provided bound parameter is null
     */
    VoidResult<F> flatMap(Supplier<? extends VoidResult<? extends F>> bound);

    /**
     * Result monad bind bridge function.
     * <p>
     * compose the provided bound supplier to the current success if any.
     * If current state is a failure, the provided bound function is not called.
     *
     * @param bound the supplier to compose current result with
     * @param <R>   the new success type
     * @return  a result containing either the R success value if current and bound function result are successes,
     *          a F-typed failure otherwise.
     * @throws NullPointerException if provided bound parameter is null
     */
    <R> Result<R, F> flatMapToResult(Supplier<? extends Result<? extends R, ? extends F>> bound);

    /**
     * try to recover from the current failure, if any.
     * The provided recovering function may also fail, making this an attempt at recovering.
     * <p>
     * If current state is a success, it is returned.
     *
     * @param recoveringFunction function to apply to the current failure if current state is a failure.
     * @return the current success or the result of the recovering function applied to the current failure.
     * @throws NullPointerException if provided recoveringFunction parameter is null
     */
    VoidResult<F> tryRecovering(Function<? super F, ? extends VoidResult<? extends F>> recoveringFunction);

    /**
     * try to recover from the current failure, if any.
     * The provided recovering function may also fail, making this an attempt at recovering.
     * <p>
     * If current state is a success, it is returned.
     *
     * @param recoveringSupplier supplier to execute if current state is a failure.
     * @return the current success, otherwise the result of the recovering supplier.
     * @throws NullPointerException if provided recoveringSupplier parameter is null
     */
    VoidResult<F> tryRecovering(Supplier<? extends VoidResult<? extends F>> recoveringSupplier);
}
