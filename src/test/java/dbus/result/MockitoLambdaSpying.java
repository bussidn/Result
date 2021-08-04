package dbus.result;

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
}
