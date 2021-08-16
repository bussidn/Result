package dbus.result;

import dbus.result.void_.VoidResult;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Class representing either a success or a failure.
 * <p>
 * Result is considered as a Union type emulation of :
 * <p>
 * Result = Success | Failure
 * <p>
 * This interface is not intended to be extended outside this library.
 *
 * @param <S> the success type
 * @param <F> the failure type
 */
public interface Result<S, F> {

    /**
     * Static factory to create a success
     *
     * @param value the value of the success.
     * @param <S>   the success type.
     * @param <F>   the failure type.
     * @return a success of type S
     * @throws NullPointerException when provided value is null
     */
    static <S, F> Success<S, F> success(S value) {
        return Success.success(value);
    }

    /**
     * Static factory to create a failure
     *
     * @param value the value of the failure.
     * @param <S>   the success type.
     * @param <F>   the failure type.
     * @return a failure of type F
     * @throws NullPointerException when provided value is null
     */
    static <S, F> Failure<S, F> failure(F value) {
        return Failure.failure(value);
    }

    /**
     * Narrow the scope of success and failure types to a supertype of the provided result.
     *
     * @param result the result that must be narrows
     * @param <S> the desired success type
     * @param <F> the desired failure type
     * @return a narrowed result
     * @throws NullPointerException if provided result is null
     */
    static <S, F> Result<S, F> narrow(Result<? extends S, ? extends F> result) {
        return result.match(Result::success, Result::failure);
    }

    /**
     * Pattern matching emulation on Result deconstructed subtypes.
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
            Function<? super S, ? extends R> success,
            Function<? super F, ? extends R> failure
    );

    /**
     * Result functor map function.
     * <p>
     * It applies the provided mapper to the success value if this is a success.
     * Returns the current failure otherwise.
     *
     * @param mapper the mapper to apply to the success
     * @param <R>    the new success return type
     * @return a Result containing either a mapped success or the current failure
     * @throws NullPointerException when provided mapper is null
     */
    <R> Result<R, F> map(Function<? super S, ? extends R> mapper);

    /**
     * bridge function from Result functor to VoidResult.
     * <p>
     * It applies the provided consumer to the success value if this is a success.
     * Returns the current failure otherwise as a VoidResult.Failure.
     *
     * @param consumer the consumer to apply to the success
     * @return a VoidResult containing either a void success or the current failure
     * @throws NullPointerException when provided consumer is null
     */
    VoidResult<F> map(Consumer<? super S> consumer);

}
