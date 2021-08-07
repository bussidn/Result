package dbus.result;

import java.util.Objects;
import java.util.function.Function;

/**
 * function extension that allows to provide Result specific composition methods
 *
 * @param <T> The type of the input of the function
 * @param <S> The type of the return type success of the function
 * @param <F> The type of the return type failure of the function
 */
@FunctionalInterface
public interface ResultFunction<T, S, F> extends Function<T, Result<S, F>> {


    static <T, S, F> ResultFunction<T, S, F> asResultFunction(Function<? super T, ? extends Result<? extends S, ? extends F>> f) {
        Objects.requireNonNull(f);
        return t -> Result.narrow(f.apply(t));
    }

}
