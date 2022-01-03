package dbus.result;

import dbus.result.void_.VoidResult;

import java.util.Optional;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;

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
     * @param <S>    the desired success type
     * @param <F>    the desired failure type
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
     * Result functor map function.
     * <p>
     * It applies the provided mapper to the success value if this is a success.
     * Returns the current failure otherwise.
     * <p>
     * similar to {@link Result#map(Function)} but the current success value is ignored.
     *
     * @param mapper the mapper to apply to the success
     * @param <R>    the new success return type
     * @return a Result containing either a mapped success or the current failure
     * @throws NullPointerException when provided mapper is null
     */
    <R> Result<R, F> map(Supplier<? extends R> mapper);

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

    /**
     * Result bifunctor map function.
     * <p>
     * It applies the provided mapper to the failure value if this is a failure.
     * Returns the current success otherwise.
     *
     * @param mapper the mapper to apply to the failure
     * @param <G>    the new success return type
     * @return a Result containing either the current success or a mapped failure
     * @throws NullPointerException when provided mapper is null
     */
    <G> Result<S, G> mapFailure(Function<? super F, ? extends G> mapper);

    /**
     * Result bifunctor map function.
     * <p>
     * It executes the provided mapper if this is a failure and keep the provided value as the new failure.
     * Returns the current success otherwise.
     *
     * @param mapper the mapper to apply to the failure
     * @param <G>    the new success return type
     * @return a Result containing either the current success or a supplied failure
     * @throws NullPointerException when provided mapper is null
     */
    <G> Result<S, G> mapFailure(Supplier<? extends G> mapper);

    /**
     * bridge function from Result to Optional
     * <p>
     * It applies the provided consumer to the failure value if this is a failure.
     * Returns the current success otherwise as an Optional.
     *
     * @param consumer the consumer to apply to the failure
     * @return an Optional containing maybe the success
     * @throws NullPointerException when provided consumer is null
     */
    Optional<S> mapFailure(Consumer<? super F> consumer);

    /**
     * Result monad bind function
     * <p>
     * compose the provided bound function to the current success if any.
     * If current state is a failure, the provided bound function is not called.
     *
     * @param bound the function to compose current result with
     * @param <R>   the new success type
     * @return a result containing either the R success value if current and bound function result are successes,
     * a F-typed failure otherwise.
     * @throws NullPointerException if provided bound parameter is null
     */
    <R> Result<R, F> flatMap(Function<? super S, ? extends Result<? extends R, ? extends F>> bound);

    /**
     * Result monad bind function but with a function that does not need the present success state.
     * <p>
     * compose the provided bound supplier to the current success if any, discarding its value.
     * If current state is a failure, the provided bound function is not called.
     *
     * @param bound the supplier to compose current result with
     * @param <R>   the new success type
     * @return a result containing either the R success value if current and bound function result are successes,
     * a F-typed failure otherwise.
     * @throws NullPointerException if provided bound parameter is null
     */
    <R> Result<R, F> flatMap(Supplier<? extends Result<? extends R, ? extends F>> bound);

    /**
     * Result monad bind bridge function with a function that returns a {@link VoidResult}.
     * <p>
     * compose the provided bound function to the current success if any, discarding its value.
     * If current state is a failure, the provided bound function is not called.
     *
     * @param bound the function to compose current result with
     * @return a result containing either the R success value if current and bound function result are successes,
     * a F-typed failure otherwise.
     * @throws NullPointerException if provided bound parameter is null
     */
    VoidResult<F> flatMapToVoid(Function<? super S, ? extends VoidResult<? extends F>> bound);

    /**
     * Result monad bind bridge function with a supplier that returns a {@link VoidResult}.
     * <p>
     * compose the provided bound supplier to the current success if any, discarding its value.
     * If current state is a failure, the provided bound function is not called.
     *
     * @param bound the supplier to compose current result with
     * @return a result containing either the R success value if current and bound function result are successes,
     * a F-typed failure otherwise.
     * @throws NullPointerException if provided bound parameter is null
     */
    VoidResult<F> flatMapToVoid(Supplier<? extends VoidResult<? extends F>> bound);

    /**
     * recover from the current failure, if any.
     * <p>
     * If current state is a success, returns the curren success.
     *
     * @param recoveringFunction function to apply to the current failure if current state is a failure.
     * @return the current success or the result of the recovering function
     * @throws NullPointerException if provided recoveringFunction parameter is null
     */
    S recover(Function<? super F, ? extends S> recoveringFunction);

    /**
     * recover from the current failure, if any.
     * <p>
     * If current state is a success, returns the curren success.
     *
     * @param recoveringSupplier to apply to the current failure if current state is a failure.
     * @return the current success or the result of the recovering function
     * @throws NullPointerException if provided recoveringSupplier parameter is null
     */
    S recover(Supplier<? extends S> recoveringSupplier);

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
    Result<S, F> tryRecovering(Function<? super F, ? extends Result<? extends S, ? extends F>> recoveringFunction);

    /**
     * try to recover from the current failure, if any.
     * The provided recovering function may also fail, making this an attempt at recovering.
     * <p>
     * If current state is a success, it is returned.
     *
     * @param recoveringSupplier supplier to execute if current state is a failure.
     * @return the current success, otherwise the result of the recovering supplier.
     * @throws NullPointerException if provided recoveringFunction parameter is null
     */
    Result<S, F> tryRecovering(Supplier<? extends Result<? extends S, ? extends F>> recoveringSupplier);

    /**
     * creates a collector that helps to reduce a result stream into a single result
     * <p>
     * The original stream is basically split into successes and failures, then you can provide the reduction strategy
     * function to reduce those results into a single result. <p>
     * <p>
     * {@link Results} contains static utility method to help create such a function : <p>
     * * {@link Results#successIf(Predicate)} <P>
     * * {@link Results#failureIf(Predicate)} <p>
     * <p>
     * that can both be combined with either of : <p>
     * * {@link Results#anySuccess()} <p>
     * * {@link Results#noSuccess()} <p>
     * * {@link Results#anyFailure()} <p>
     * * {@link Results#noFailure()} <p>
     * <p>
     * For example, you can decide that your result stream is a success if there is no failure with
     * <p>
     * <code> Results.successIf(Results.noFailure())</code>
     * <p>
     * or you can decide that your result stream is a failure if there is any failure
     * <p>
     * <code> Results.failureIf(Results.anyFailure())</code>
     * <p>
     * Or you can provide you own implementation, as all those parameters are Functions that can be provided in a lambda
     * form.
     *
     * @param reductionStrategy function that is applied once all results have been collected
     * @param <S>               the result stream success type
     * @param <F>               the result stream failure type
     * @param <NS>              the new success type
     * @param <NF>              the new failure type
     * @return the reduced result
     */
    static <S, F, NS, NF> Collector<Result<S, F>, ?, Result<NS, NF>>
    collector(final Function<Results<S, F>, Result<NS, NF>> reductionStrategy) {

        return new Collector<Result<S, F>, Results<S, F>, Result<NS, NF>>() {

            @Override
            public Supplier<Results<S, F>> supplier() {
                return Results::new;
            }

            @Override
            public BiConsumer<Results<S, F>, Result<S, F>> accumulator() {
                return Results::add;
            }

            @Override
            public BinaryOperator<Results<S, F>> combiner() {
                return Results::addAll;
            }

            @Override
            public Function<Results<S, F>, Result<NS, NF>> finisher() {
                return reductionStrategy;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of();
            }
        };
    }
}
