package dbus.result.void_;

import dbus.result.Result;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Result that does not have a success value
 */
public interface VoidResult<F> {

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
}
