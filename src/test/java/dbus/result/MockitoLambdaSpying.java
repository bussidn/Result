package dbus.result;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;

public class MockitoLambdaSpying {

    /**
     * This method overcomes the issue with the original Mockito.spy when passing a lambda which fails with an error
     * saying that the passed class is final.
     */
    @SuppressWarnings("unchecked") public static <R, G extends R> G spyLambda(final G lambda, final Class<R> lambdaType) {
         return (G) mock(lambdaType, delegatesTo(lambda));
     }

    /**
     * specialisation of {@link MockitoLambdaSpying#spyLambda(Object, Class)} for {@link Runnable}
     */
    public static Runnable spiedRunnable() {
         return spyLambda(() -> {}, Runnable.class);
     }

    /**
     * specialisation of {@link MockitoLambdaSpying#spyLambda(Object, Class)} for {@link Supplier}
     */
    public static <R> Supplier<R> spiedSupplier(Supplier<R> lambda) {
         return spyLambda(lambda, Supplier.class);
     }

     /**
     * specialisation of {@link MockitoLambdaSpying#spyLambda(Object, Class)} for {@link Function}
     */
    public static <T, R> Function<T, R> spiedFunction(Function<T, R> lambda) {
         return spyLambda(lambda, Function.class);
     }
}
