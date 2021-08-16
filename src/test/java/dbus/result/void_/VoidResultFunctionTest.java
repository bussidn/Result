package dbus.result.void_;

import dbus.result.Result;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.function.Supplier;

import static dbus.result.MockitoLambdaSpying.spiedRunnable;
import static dbus.result.MockitoLambdaSpying.spiedSupplier;
import static dbus.result.void_.VoidResult.failure;
import static dbus.result.void_.VoidResult.success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class VoidResultFunctionTest {

    @Nested
    class Conversion {

        @Test
        public void asVoidResultFunction_should_not_accept_null_input() {
            //noinspection ResultOfMethodCallIgnored
            assertThrows(NullPointerException.class, () ->
                    VoidResultFunction.<Integer, Object>asVoidResultFunction(null));
        }

        @Test
        public void asVoidResultFunction_should_convert_a_returning_void_result_function_to_a_void_result_function() {
            // given
            Function<Number, VoidResult<String>> f = n -> VoidResult.failure(n.toString());

            // when
            VoidResultFunction<Integer, Object> converted = VoidResultFunction.asVoidResultFunction(f);

            // then
            assertThat(converted.apply(72 )).isEqualTo(f.apply(72));
        }
    }



    @Nested
    class Map {

        @Test
        public void map_runnable_should_be_provided_with_non_null_mapper() {
            // given
            VoidResultFunction<String, String> resultFunction = VoidResult::failure;

            // then
            assertThrows(NullPointerException.class, () ->
                    resultFunction.map((Runnable) null)
            );
        }

        @Test
        public void map_runnable_should_return_the_failure_when_composing_void_result_function_returns_a_failure() {
            // given
            VoidResultFunction<String, String> resultFunction = VoidResult::failure;
            Runnable runnable = () -> {};

            // when
            var composed = resultFunction.map(runnable);

            // then
            assertThat(composed.apply("because")).isEqualTo(failure("because"));
        }

        @Test
        public void map_runnable_should_not_execute_the_provided_runnable_when_composing_void_result_function_returns_a_failure() {
            // given
            VoidResultFunction<String, String> resultFunction = VoidResult::failure;
            Runnable runnable = spiedRunnable();

            // when
            resultFunction.map(runnable).apply("should not run the runnable");

            // then
            verify(runnable, never()).run();
        }

        @Test
        public void map_runnable_should_return_a_success_when_composing_void_result_function_returns_a_success() {
            // given
            VoidResultFunction<String, String> resultFunction = s -> success();
            Runnable runnable = () -> {};

            // when
            var composed = resultFunction.map(runnable);

            // then
            assertThat(composed.apply("should lead to a void success")).isEqualTo(success());
        }

        @Test
        public void map_runnable_should_execute_the_provided_runnable_when_composing_void_result_function_returns_a_success() {
            // given
            VoidResultFunction<String, String> resultFunction = s -> success();
            Runnable runnable = spiedRunnable();

            // when
            resultFunction.map(runnable).apply("should run runnable");

            // then
            verify(runnable).run();
        }

        @Test
        public void map_supplier_should_be_provided_with_non_null_consumer() {
            // given
            VoidResultFunction<String, String> resultFunction = VoidResult::failure;

            // then
            assertThrows(NullPointerException.class, () ->
                    resultFunction.map((Supplier<?>) null)
            );
        }

        @Test
        public void map_supplier_should_return_the_failure_when_composing_void_result_function_returns_a_failure() {
            // given
            VoidResultFunction<String, String> resultFunction = VoidResult::failure;
            Supplier<String> supplier = () -> "success";

            // when
            var composed = resultFunction.map(supplier);

            // then
            assertThat(composed.apply("because")).isEqualTo(Result.failure("because"));
        }

        @Test
        public void map_supplier_should_not_execute_the_provided_supplier_when_composing_void_result_function_returns_a_failure() {
            // given
            VoidResultFunction<String, String> resultFunction = VoidResult::failure;
            Supplier<String> supplier = spiedSupplier(() -> "success");

            // when
            resultFunction.map(supplier).apply("should not execute the supplier");

            // then
            verify(supplier, never()).get();
        }

        @Test
        public void map_supplier_should_return_a_success_when_composing_void_result_function_returns_a_success() {
            // given
            VoidResultFunction<String, String> resultFunction = s -> success();
            Supplier<String> supplier = () -> "success";

            // when
            var composed = resultFunction.map(supplier);

            // then
            assertThat(composed.apply("should lead to a success")).isEqualTo(Result.success("success"));
        }

        @Test
        public void map_supplier_should_execute_the_provided_supplier_when_composing_void_result_function_returns_a_success() {
            // given
            VoidResultFunction<String, String> resultFunction = s -> success();
            Supplier<String> supplier = spiedSupplier(() -> "success");

            // when
            resultFunction.map(supplier).apply("should run supplier");

            // then
            verify(supplier).get();
        }
    }

}