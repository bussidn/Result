package dbus.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * class that represent multiple results
 *
 * @param <S> the success type
 * @param <F> the failure type
 */
public class Results<S, F> {

    private final List<S> successes = new ArrayList<>();
    private final List<F> failures = new ArrayList<>();

    protected void add(Result<S, F> result) {
        result.match(
                this::addSuccess,
                this::addFailure
        );
    }

    private Results<S, F> addSuccess(S left) {
        successes.add(left);
        return this;
    }

    private Results<S, F> addFailure(F right) {
        failures.add(right);
        return this;
    }

    protected Results<S, F> addAll(Results<S, F> other) {
        successes.addAll(other.successes);
        failures.addAll(other.failures);
        return this;
    }

    /**
     * utility method that helps to create the reduction function necessary for {@link Result#collector(Function)}
     * function
     * <p>
     * This will return a success or a failure function based on the provided predicate.
     *
     * @param predicate predicate to apply to know if the result will be a success or a failure
     * @param <S>       the success type
     * @param <F>       the failure type
     * @return a reduction function that can be applied by the collector
     * @see Result#collector(Function) collector for usage example
     */
    public static <S, F> Function<Results<S, F>, Result<Collection<S>, Collection<F>>> successIf(Predicate<Results<S, F>> predicate) {
        return results -> predicate.test(results) ?
                Result.success(new ArrayList<S>(results.successes)) :
                Result.failure(new ArrayList<F>(results.failures));
    }


    /**
     * utility method that helps to create the reduction function necessary for {@link Result#collector(Function)}
     * function
     * <p>
     * This will return a success or a failure function based on the provided predicate.
     *
     * @param predicate predicate to apply to know if the result will be a success or a failure
     * @param <S>       the success type
     * @param <F>       the failure type
     * @return a reduction function that can be applied by the collector
     * @see Result#collector(Function) collector for usage example
     */
    public static <S, F> Function<Results<S, F>, Result<Collection<S>, Collection<F>>> failureIf(Predicate<Results<S, F>> predicate) {
        return successIf(predicate.negate());
    }

    /**
     * utility method that helps to create the reduction function necessary for {@link Result#collector(Function)}
     * function
     * <p>
     * this will create a predicate that can be used by {@link Results#successIf(Predicate)} or
     * {@link Results#failureIf(Predicate)} functions to generate the reduction function
     *
     * @param <S> the success type
     * @param <F> the failure type
     * @return a predicate function
     * @see Result#collector(Function) collector for usage example
     */
    public static <S, F> Predicate<Results<S, F>> noSuccess() {
        return results -> results.successes.isEmpty();
    }


    /**
     * utility method that helps to create the reduction function necessary for {@link Result#collector(Function)}
     * function
     * <p>
     * this will create a predicate that can be used by {@link Results#successIf(Predicate)} or
     * {@link Results#failureIf(Predicate)} functions to generate the reduction function
     *
     * @param <S> the success type
     * @param <F> the failure type
     * @return a predicate function
     * @see Result#collector(Function) collector for usage example
     */
    public static <S, F> Predicate<Results<S, F>> anySuccess() {
        return Results.<S, F>noSuccess().negate();
    }


    /**
     * utility method that helps to create the reduction function necessary for {@link Result#collector(Function)}
     * function
     * <p>
     * this will create a predicate that can be used by {@link Results#successIf(Predicate)} or
     * {@link Results#failureIf(Predicate)} functions to generate the reduction function
     *
     * @param <S> the success type
     * @param <F> the failure type
     * @return a predicate function
     * @see Result#collector(Function) collector for usage example
     */
    public static <S, F> Predicate<Results<S, F>> noFailure() {
        return results -> results.failures.isEmpty();
    }


    /**
     * utility method that helps to create the reduction function necessary for {@link Result#collector(Function)}
     * function
     * <p>
     * this will create a predicate that can be used by {@link Results#successIf(Predicate)} or
     * {@link Results#failureIf(Predicate)} functions to generate the reduction function
     *
     * @param <S> the success type
     * @param <F> the failure type
     * @return a predicate function
     * @see Result#collector(Function) collector for usage example
     */
    public static <S, F> Predicate<Results<S, F>> anyFailure() {
        return Results.<S, F>noFailure().negate();
    }

    public List<S> successes() {
        return new ArrayList<>(successes);
    }

    public List<F> failures() {
        return new ArrayList<>(failures);
    }

}
