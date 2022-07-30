package dbus.result.void_;

import dbus.result.Result;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static dbus.result.MockitoLambdaSpying.*;
import static dbus.result.void_.VoidResult.failure;
import static dbus.result.void_.VoidResult.success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
            assertThat(converted.apply(72)).isEqualTo(f.apply(72));
        }
    }


    @Nested
    class Map {

        @Nested
        class SuccessSide {

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
                Runnable runnable = () -> {
                };

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
                Runnable runnable = () -> {
                };

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
        class FailureSide {

            @Test
            @DisplayName("Failure (Function) should be provided with non null mapper")
            public void mapFailure_function_should_be_provided_with_non_null_mapper() {
                // given
                VoidResultFunction<String, String> resultFunction = s -> VoidResult.success();

                // then
                assertThrows(NullPointerException.class, () ->
                        resultFunction.mapFailure((Function<String, Integer>) null)
                );
            }

            @Test
            @DisplayName("mapFailure (Function) composed function should execute provided mapper when initial result is a failure")
            public void mapFailure_function_composed_function_should_execute_provided_mapper_when_initial_result_is_a_failure() {
                // given
                VoidResultFunction<String, String> failingInitialFunction = VoidResult::failure;
                Function<String, Integer> mapper = String::length;

                // when
                var composed = failingInitialFunction.mapFailure(mapper);

                // then
                AssertionsForClassTypes.assertThat(composed.apply("should be 12")).isEqualTo(VoidResult.failure(12));
            }

            @Test
            @DisplayName("mapFailure (Function) composed function should return initial result when initial result is a success")
            public void mapFailure_function_composed_function_should_return_initial_result_when_initial_result_is_a_success() {
                // given
                VoidResultFunction<String, String> successfulInitialFunction = s -> VoidResult.success();
                Function<String, Integer> mapper = String::length;

                // when
                var composed = successfulInitialFunction.mapFailure(mapper);

                // then
                AssertionsForClassTypes.assertThat(composed.apply("should succeed")).isEqualTo(VoidResult.<String>success());
            }

            @Test
            @DisplayName("mapFailure (Function) composed function should not execute provided mapper when initial result is a success")
            public void mapFailure_function_composed_function_should_not_execute_provided_mapper_when_initial_result_is_a_success() {
                // given
                VoidResultFunction<String, String> successfulInitialFunction = s -> VoidResult.success();
                Function<String, Integer> spiedFunction = spiedFunction(String::length);

                // when
                successfulInitialFunction.mapFailure(spiedFunction);

                // then
                verify(spiedFunction, never()).apply(any());
            }

            @Test
            @DisplayName("mapFailure (Supplier) should be provided with non null supplier")
            public void mapFailure_supplier_should_be_provided_with_non_null_supplier() {
                // given
                VoidResultFunction<String, String> resultFunction = s -> VoidResult.success();

                // then
                assertThrows(NullPointerException.class, () ->
                        resultFunction.mapFailure((Supplier<Integer>) null)
                );
            }

            @Test
            @DisplayName("mapFailure (Supplier) composed function should execute provided mapper when initial result is a failure")
            public void mapFailure_supplier_composed_function_should_execute_provided_mapper_when_initial_result_is_a_failure() {
                // given
                VoidResultFunction<String, String> failingInitialFunction = VoidResult::failure;
                Supplier<Integer> mapper = () -> 17;

                // when
                var composed = failingInitialFunction.mapFailure(mapper);

                // then
                AssertionsForClassTypes.assertThat(composed.apply("should be 17")).isEqualTo(VoidResult.failure(17));
            }

            @Test
            @DisplayName("mapFailure (Supplier) composed function should return initial result when initial result is a success")
            public void mapFailure_supplier_composed_function_should_return_initial_result_when_initial_result_is_a_success() {
                // given
                VoidResultFunction<String, String> successfulInitialFunction = s -> VoidResult.success();
                Supplier<String> mapper = () -> "should not matter";

                // when
                var result = successfulInitialFunction.mapFailure(mapper);

                // then
                AssertionsForClassTypes.assertThat(result.apply("should succeed")).isEqualTo(VoidResult.<String>success());
            }

            @Test
            @DisplayName("mapFailure (Supplier) composed function should not execute provided mapper when initial result is a success")
            public void mapFailure_supplier_composed_function_should_not_execute_provided_mapper_when_initial_result_is_a_success() {
                // given
                VoidResultFunction<String, String> successfulInitialFunction = s -> VoidResult.success();
                Supplier<Integer> spiedSupplier = spiedSupplier(() -> 5456);

                // when
                successfulInitialFunction.mapFailure(spiedSupplier);

                // then
                verify(spiedSupplier, never()).get();
            }

            @Test
            @DisplayName("mapFailure (Consumer) should be provided with non null consumer")
            public void mapFailure_consumer_should_be_provided_with_non_null_consumer() {
                // given
                VoidResultFunction<String, String> resultFunction = s -> VoidResult.success();

                // then
                assertThrows(NullPointerException.class, () ->
                        resultFunction.mapFailure((Consumer<String>) null)
                );
            }

            @Test
            @DisplayName("mapFailure (Consumer) composed function should execute provided consumer when initial result is a failure")
            public void mapFailure_consumer_composed_function_should_execute_provided_consumer_when_initial_result_is_a_failure() {
                // given
                VoidResultFunction<String, String> failingInitialFunction = VoidResult::failure;
                Consumer<String> consumer = spiedConsumer();

                // when
                failingInitialFunction.mapFailure(consumer).accept("anyThing");

                // then
                verify(consumer).accept(any());
            }

            @Test
            @DisplayName("mapFailure (Consumer) composed function should call provided consumer when initial result is a success")
            public void mapFailure_consumer_composed_function_should_not_execute_provided_consumer_when_initial_result_is_a_success() {
                // given
                VoidResultFunction<String, String> successfulInitialFunction = s -> VoidResult.success();
                Consumer<String> consumer = spiedConsumer();

                // when
                successfulInitialFunction.mapFailure(consumer).accept("anything");

                // then
                verify(consumer, times(0)).accept(any());
            }

            @Test
            @DisplayName("mapFailure (Consumer) composed function should not execute provided mapper when initial result is a success")
            public void mapFailure_consumer_composed_function_should_not_execute_provided_mapper_when_initial_result_is_a_success() {
                // given
                VoidResultFunction<String, String> successfulInitialFunction = s -> VoidResult.success();
                Consumer<String> spiedConsumer = spiedConsumer();

                // when
                successfulInitialFunction.mapFailure(spiedConsumer);

                // then
                verify(spiedConsumer, never()).accept(any());
            }
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
            failure.flatMap(should_not_be_executed).apply("test");

            // then
            verify(should_not_be_executed, never()).get();
        }

        @Nested
        @TestInstance(PER_CLASS)
        class ToResult {

            @Test
            public void flatMapToResult_should_not_accept_null_parameter() {
                assertThrows(NullPointerException.class, () -> (
                        (VoidResultFunction<String, String>) VoidResult::failure)
                        .flatMapToResult((Supplier<Result<Integer, String>>) null)
                );
            }

            @ParameterizedTest(name = "flatMapToResult should apply the provided bound supplier when initial result is a success")
            @MethodSource("boundSupplier")
            public void flatMapToVoid_supplier_should_apply_the_provided_bound_supplier_when_initial_result_is_a_success(
                    Supplier<Result<Integer, String>> boundSupplier
            ) {
                // given
                VoidResultFunction<String, String> success = s -> VoidResult.success();

                // when
                var flatMappedFunction = success.flatMapToResult(boundSupplier);

                // then
                Assertions.assertThat(flatMappedFunction.apply("useless")).isEqualTo(boundSupplier.get());
            }

            Stream<Arguments> boundSupplier() {
                return Stream.of(
                        Arguments.of(
                                (Supplier<Result<Integer, String>>) () -> Result.success(333)
                        ),
                        Arguments.of(
                                (Supplier<Result<Integer, String>>) () -> Result.failure("because")
                        )
                );
            }

            @Test
            public void flatMapToResult_should_return_the_initial_failure_when_initial_result_is_a_failure() {
                // given
                VoidResultFunction<String, String> failure = s -> VoidResult.failure("already failed : " + s);
                Supplier<Result<Integer, String>> should_not_be_executed = () -> Result.failure("should not be executed");

                // when
                var flatMappedResult = failure.flatMapToResult(should_not_be_executed);

                // then
                Assertions.assertThat(flatMappedResult.apply("because")).isEqualTo(Result.failure("already failed : because"));
            }

            @Test
            public void flatMapToResult_should_not_execute_provided_bound_supplier_when_initial_result_is_a_failure() {
                // given
                VoidResultFunction<String, String> failure = s -> VoidResult.failure("already failed : " + s);
                Supplier<Result<Integer, String>> should_not_be_executed = spiedSupplier(() -> Result.failure("should not be executed"));

                // when
                failure.flatMapToResult(should_not_be_executed).apply("test");

                // then
                verify(should_not_be_executed, never()).get();
            }

        }

    }

    @Nested
    @TestInstance(PER_CLASS)
    class Recover {

        @DisplayName("thenTryRecovering (Function) should not accept null parameter")
        @Test
        public void thenTryRecovering_function_should_not_accept_null_parameter() {
            assertThrows(NullPointerException.class, () ->
                    ((VoidResultFunction<Object, String>) o -> null).thenTryRecovering((Function<Object, VoidResult<String>>) null)
            );
        }

        @Test
        @DisplayName("thenTryRecovering (Function) should return the initial success when initial result is a success")
        public void thenTryRecovering_function_should_return_the_initial_success_when_initial_result_is_a_success() {
            // given
            VoidResultFunction<Object, String> initialFunction = o -> VoidResult.success();

            // when
            var recovered =
                    initialFunction.thenTryRecovering(s -> VoidResult.failure("should not be executed"))
                            .apply(new Object());

            // then
            assertThat(recovered).isEqualTo(VoidResult.success());
        }

        @Test
        @DisplayName("thenTryRecovering (Function) should not execute provided recovering function when initial result is a success")
        public void thenTryRecovering_function_should_not_execute_provided_recovering_function_when_initial_result_is_a_success() {
            // given
            VoidResultFunction<Object, String> initialFunction = o -> VoidResult.success();
            Function<String, VoidResult<String>> should_not_be_executed = spiedFunction(s -> VoidResult.success());

            // when
            initialFunction.thenTryRecovering(should_not_be_executed);

            // then
            verify(should_not_be_executed, never()).apply(any());
        }


        @ParameterizedTest(name = "thenTryRecovering (Function) should return the recovering function result when initial result is a failure : {0}")
        @MethodSource("successAndFailure")
        public void thenTryRecovering_function_should_return_the_new_success_when_initial_result_is_a_failure(
                final VoidResult<String> recoveringFunctionResult
        ) {
            // given
            VoidResultFunction<Integer, String> initialFunction = i -> VoidResult.failure("failed so badly but it does not matter !");

            // when
            var recovered =
                    initialFunction.thenTryRecovering(s -> recoveringFunctionResult)
                            .apply(13);

            // then
            assertThat(recovered).isEqualTo(recoveringFunctionResult);
        }

        @DisplayName("thenTryRecovering (Supplier) should not accept null parameter")
        @Test
        public void thenTryRecovering_supplier_should_not_accept_null_parameter() {
            assertThrows(NullPointerException.class, () ->
                    ((VoidResultFunction<Object, String>) o -> null).thenTryRecovering((Supplier<VoidResult<String>>) null)
            );
        }

        @Test
        @DisplayName("thenTryRecovering (Supplier) should return the initial success when initial result is a success")
        public void thenTryRecovering_supplier_should_return_the_initial_success_when_initial_result_is_a_success() {
            // given
            VoidResultFunction<Object, String> initialFunction = o -> VoidResult.success();

            // when
            var recovered =
                    initialFunction.thenTryRecovering(() -> VoidResult.failure("should not be executed"))
                            .apply(new Object());

            // then
            assertThat(recovered).isEqualTo(VoidResult.success());
        }

        @Test
        @DisplayName("thenTryRecovering (Supplier) should not execute provided recovering function when initial result is a success")
        public void thenTryRecovering_supplier_should_not_execute_provided_recovering_function_when_initial_result_is_a_success() {
            // given
            VoidResultFunction<Object, String> initialFunction = o -> VoidResult.success();
            Supplier<VoidResult<String>> should_not_be_executed = spiedSupplier(() -> VoidResult.failure("should not be executed"));

            // when
            initialFunction.thenTryRecovering(should_not_be_executed);

            // then
            verify(should_not_be_executed, never()).get();
        }

        @ParameterizedTest(name = "thenTryRecovering (Supplier) should return the recovering supplier result when initial result is a failure : {0}")
        @MethodSource("successAndFailure")
        public void thenTryRecovering_supplier_should_return_the_new_success_when_initial_result_is_a_failure(
                final VoidResult<String> recoveringSupplierResult
        ) {
            // given
            VoidResultFunction<Integer, String> initialFunction = i -> VoidResult.failure("failed so badly but it does not matter !");

            // when
            var recovered =
                    initialFunction.thenTryRecovering(() -> recoveringSupplierResult)
                            .apply(88);

            // then
            assertThat(recovered).isEqualTo(recoveringSupplierResult);
        }

        Stream<Arguments> successAndFailure() {
            return VoidResultTest.successAndFailure();
        }
    }

}