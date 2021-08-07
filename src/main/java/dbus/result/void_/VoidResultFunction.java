package dbus.result.void_;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface VoidResultFunction<T, F> extends Function<T, VoidResult<F>> {


    static <T, F> VoidResultFunction<T, F> asVoidResultFunction(Function<? super T, ? extends VoidResult<? extends F>> f) {
        Objects.requireNonNull(f);
        return t -> VoidResult.narrow(f.apply(t));
    }
}
