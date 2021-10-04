package dbus.result.void_;

import dbus.result.Result;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static dbus.result.MockitoLambdaSpying.spiedRunnable;
import static dbus.result.MockitoLambdaSpying.spiedSupplier;
import static dbus.result.void_.VoidResult.failure;
import static dbus.result.void_.VoidResult.success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
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



    @Nested
    @TestInstance(PER_CLASS)
    class FlatMap {

        @Test
        public void flatMap_function_should_not_accept_null_parameter() {
            assertThrows(NullPointerException.class, () ->
                    ((VoidResultFunction<String, String>) s -> null).flatMap(null)
            );
        }

        @ParameterizedTest(name = "flatMap should return {0} when initial result is a success")
        @MethodSource("boundSupplier")
        public void flatMap_supplier_should_apply_the_provided_bound_supplier_when_initial_result_is_a_success(
                final VoidResult<String> boundedResult
        ) {
            // given
            VoidResultFunction<String, String> success = s -> VoidResult.success();

            // when
            var flatMappedResult = success.flatMap(() -> boundedResult);

            // then
            Assertions.assertThat(flatMappedResult.apply("test")).isEqualTo(boundedResult);
        }

        Stream<Arguments> boundSupplier() {
            return Stream.of(
                    Arguments.of(VoidResult.<String>success()),
                    Arguments.of(VoidResult.failure("because"))
            );
        }

        @Test
        public void flatMap_function_should_not_execute_provided_bound_function_when_initial_result_is_a_failure() {
            // given
            VoidResultFunction<String, String> failure = s -> VoidResult.failure("already failed : " + s);
            Supplier<VoidResult<String>> should_not_be_executed = spiedSupplier(() -> VoidResult.failure("should not be executed"));

            // when
            failure.flatMap(should_not_be_executed);

            // then
            verify(should_not_be_executed, never()).get();
        }

        Stream<Arguments> bound() {
            return Stream.of(
                    Arguments.of(
                            (Supplier<VoidResult<String>>) VoidResult::success
                    ),
                    Arguments.of(
                            (Supplier<VoidResult<String>>) () -> VoidResult.failure("because")
                    )
            );
        }

    }

}