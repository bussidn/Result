package dbus.result;

import dbus.result.void_.VoidResult;
import dbus.result.void_.VoidResultFunction;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static dbus.result.MockitoLambdaSpying.*;
import static dbus.result.Result.success;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ResultFunctionTest {

    @Nested
    class Conversion {
        @Test
        public void asResultFunction_should_not_accept_null_input() {
            //noinspection ResultOfMethodCallIgnored
            assertThrows(NullPointerException.class, () ->
                    ResultFunction.<Integer, String, Object>asResultFunction(null));
        }

        @Test
        public void asResultFunction_should_convert_a_returning_result_function_to_a_result_function() {
            // given
            Function<Number, Result<String, String>> f = n -> success(n.toString());

            // when
            ResultFunction<Integer, Object, Object> converted = ResultFunction.asResultFunction(f);

            // then
            assertThat(converted.apply(12)).isEqualTo(f.apply(12));
        }
    }

    @Nested
    class Map {

        @Test
        @DisplayName("map (Function) should be provided with non null mapper")
        public void map_function_should_be_provided_with_non_null_mapper() {
            // given
            ResultFunction<String, String, String> resultFunction = Result::success;

            // then
            assertThrows(NullPointerException.class, () ->
                    resultFunction.map((Function<String, String>) null)
            );
        }

        @Test
        @DisplayName("map (Function) composed function should execute provided mapper when initial result is a success")
        public void map_function_composed_function_should_execute_provided_mapper_when_initial_result_is_a_success() {
            // given
            ResultFunction<String, String, String> successfulInitialFunction = Result::success;
            Function<String, Integer> mapper = String::length;

            // when
            var composed = successfulInitialFunction.map(mapper);

            // then
            assertThat(composed.apply("should be 12")).isEqualTo(success(12));
        }

        @Test
        @DisplayName("map (Function) composed function should return initial result when initial result is a failure")
        public void map_function_composed_function_should_return_initial_result_when_initial_result_is_a_failure() {
            // given
            ResultFunction<String, String, String> failingInitialFunction = Result::failure;
            Function<String, Integer> mapper = String::length;

            // when
            var composed = failingInitialFunction.map(mapper);

            // then
            assertThat(composed.apply("should fail")).isEqualTo(Result.<String, String>failure("should fail"));
        }

        @Test
        @DisplayName("map (Function) composed function should not execute provided mapper when initial result is a failure")
        public void map_function_composed_function_should_not_execute_provided_mapper_when_initial_result_is_a_failure() {
            // given
            ResultFunction<String, String, String> failingInitialFunction = Result::failure;
            Function<String, Integer> spiedFunction = spiedFunction(String::length);

            // when
            failingInitialFunction.map(spiedFunction);

            // then
            verify(spiedFunction, never()).apply(any());
        }

        @Test
        @DisplayName("map (Supplier) should be provided with non null supplier")
        public void map_supplier_should_be_provided_with_non_null_supplier() {
            // given
            ResultFunction<String, String, String> resultFunction = Result::success;

            // then
            assertThrows(NullPointerException.class, () ->
                    resultFunction.map((Supplier<Integer>) null)
            );
        }

        @Test
        @DisplayName("map (Supplier) composed function should execute provided mapper when initial result is a success")
        public void map_supplier_composed_function_should_execute_provided_mapper_when_initial_result_is_a_success() {
            // given
            ResultFunction<String, String, String> successfulInitialFunction = Result::success;
            Supplier<Integer> mapper = () -> 17;

            // when
            var composed = successfulInitialFunction.map(mapper);

            // then
            assertThat(composed.apply("should be 17")).isEqualTo(success(17));
        }

        @Test
        @DisplayName("map (Supplier) composed function should return initial result when initial result is a failure")
        public void map_supplier_composed_function_should_return_initial_result_when_initial_result_is_a_failure() {
            // given
            ResultFunction<String, String, String> failingInitialFunction = Result::failure;
            Supplier<String> mapper = () -> "should not matter";

            // when
            var result = failingInitialFunction.map(mapper);

            // then
            assertThat(result.apply("should fail")).isEqualTo(Result.<String, String>failure("should fail"));
        }

        @Test
        @DisplayName("map (Supplier) composed function should not execute provided mapper when initial result is a failure")
        public void map_supplier_composed_function_should_not_execute_provided_mapper_when_initial_result_is_a_failure() {
            // given
            ResultFunction<String, String, String> failingInitialFunction = Result::failure;
            Supplier<Integer> spiedSupplier = spiedSupplier(() -> 5456);

            // when
            failingInitialFunction.map(spiedSupplier);

            // then
            verify(spiedSupplier, never()).get();
        }

        @Test
        @DisplayName("map (Consumer) should be provided with non null consumer")
        public void map_consumer_should_be_provided_with_non_null_consumer() {
            // given
            ResultFunction<String, String, String> resultFunction = Result::success;

            // then
            assertThrows(NullPointerException.class, () ->
                    resultFunction.map((Consumer<String>) null)
            );
        }

        @Test
        @DisplayName("map (Consumer) composed function should execute provided mapper when initial result is a success")
        public void map_consumer_composed_function_should_execute_provided_mapper_when_initial_result_is_a_success() {
            // given
            ResultFunction<String, String, String> successfulInitialFunction = Result::success;
            Consumer<String> consumer = s -> {};

            // when
            var composed = successfulInitialFunction.map(consumer);

            // then
            assertThat(composed.apply("should be 17")).isEqualTo(VoidResult.success());
        }

        @Test
        @DisplayName("map (Consumer) composed function should return initial result when initial result is a failure")
        public void map_consumer_composed_function_should_return_initial_result_when_initial_result_is_a_failure() {
            // given
            ResultFunction<String, String, String> failingInitialFunction = Result::failure;
            Consumer<String> mapper = s -> {};

            // when
            var result = failingInitialFunction.map(mapper);

            // then
            assertThat(result.apply("should fail")).isEqualTo(VoidResult.failure("should fail"));
        }

        @Test
        @DisplayName("map (Consumer) composed function should not execute provided mapper when initial result is a failure")
        public void map_consumer_composed_function_should_not_execute_provided_mapper_when_initial_result_is_a_failure() {
            // given
            ResultFunction<String, String, String> failingInitialFunction = Result::failure;
            Consumer<String> spiedConsumer = spiedConsumer();

            // when
            failingInitialFunction.map(spiedConsumer);

            // then
            verify(spiedConsumer, never()).accept(any());
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    class FlatMap {

        @Test
        public void flatMap_function_should_not_accept_null_parameter() {
            assertThrows(NullPointerException.class, () ->
                    ((ResultFunction<String, String, String>) s -> null).flatMap((Function<String, Result<Integer, String>>) null)
            );
        }

        @ParameterizedTest(name = "flatMapped function should return {1} when input is {0}")
        @MethodSource("flatMap")
        public void flatMap_function_should_compose_result_functions(
                String input,
                Result<Integer, String> expectedResult
        ) {
            // given
            ResultFunction<String, String, String> firstFunction = s -> s.length() < 10 ?
                    Result.success(s + " world!") : Result.failure("failed first");

            ResultFunction<String, Integer, String> secondFunction = s -> s.length() == 12 ?
                    Result.success(s.length()) : Result.failure("failed second");

            // when
            var compositionFunction = firstFunction.flatMap(secondFunction);

            // then
            Assertions.assertThat(compositionFunction.apply(input)).isEqualTo(expectedResult);
        }

        Stream<Arguments> flatMap() {
            return Stream.of(
                    Arguments.of("Hello", Result.success(12)),
                    Arguments.of("should fail first", Result.failure("failed first")),
                    Arguments.of("2", Result.failure("failed second"))
            );
        }

        @Test
        public void flatMap_function_should_not_execute_provided_bound_function_when_initial_result_is_a_failure() {
            // given
            ResultFunction<String, String, String> failure = s -> Result.failure(s + "already failed");
            Function<String, Result<Integer, String>> should_not_be_executed = spiedFunction(s -> Result.failure("should not be executed"));

            // when
            failure.flatMap(should_not_be_executed).apply("test");

            // then
            verify(should_not_be_executed, never()).apply(any());
        }

        @Test
        public void flatMap_supplier_should_not_accept_null_parameter() {
            assertThrows(NullPointerException.class, () ->
                    ((ResultFunction<String, String, String>) s -> null).flatMap((Supplier<Result<Integer, String>>) null)
            );
        }

        @ParameterizedTest(name = "flatMap (supplier) should apply the provided bound supplier when initial result is a success")
        @MethodSource("boundSupplier")
        public void flatMap_supplier_should_apply_the_provided_bound_supplier_when_initial_result_is_a_success(
                Supplier<Result<Integer, String>> boundSupplier
        ) {
            // given
            ResultFunction<String, String, String> success = Result::success;

            // when
            var flatMappedResult = success.flatMap(boundSupplier);

            // then
            Assertions.assertThat(flatMappedResult.apply("test")).isEqualTo(boundSupplier.get());
        }

        Stream<Arguments> boundSupplier() {
            return Stream.of(
                    Arguments.of(
                            (Supplier<Result<Integer, String>>) () -> Result.success(12)
                    ),
                    Arguments.of(
                            (Supplier<Result<Integer, String>>) () -> Result.failure("because")
                    )
            );
        }

        @Test
        public void flatMap_supplier_should_return_the_initial_failure_when_initial_result_is_a_failure() {
            // given
            ResultFunction<String, String, String> failure = s -> Result.failure("already failed : " + s);
            Supplier<Result<Integer, String>> should_not_be_executed = () -> Result.failure("should not be executed");

            // when
            var flatMappedResult = failure.flatMap(should_not_be_executed);

            // then
            Assertions.assertThat(flatMappedResult.apply("because")).isEqualTo(Result.failure("already failed : because"));
        }

        @Test
        public void flatMap_supplier_should_not_execute_provided_bound_supplier_when_initial_result_is_a_failure() {
            // given
            ResultFunction<String, String, String> failure = s -> Result.failure("already failed : " + s);
            Supplier<Result<Integer, String>> should_not_be_executed = spiedSupplier(() -> Result.failure("should not be executed"));

            // when
            failure.flatMap(should_not_be_executed).apply("test");

            // then
            verify(should_not_be_executed, never()).get();
        }

        @Nested
        @TestInstance(PER_CLASS)
        class ToVoidResult {

            @Test
            public void flatMapToVoid_function_should_not_accept_null_parameter() {
                assertThrows(NullPointerException.class, () ->
                        ((ResultFunction<String, String, String>) s -> null).flatMapToVoid((Function<String, VoidResult<String>>) null)
                );
            }

            @ParameterizedTest(name = "flatMapped (Function) function should return {1} when input is {0}")
            @MethodSource("flatMap")
            public void flatMapToVoid_function_should_compose_result_functions(
                    String input,
                    VoidResult<String> expectedResult
            ) {
                // given
                ResultFunction<String, String, String> firstFunction = s -> s.length() < 10 ?
                        Result.success(s + " world!") : Result.failure("failed first");

                VoidResultFunction<String, String> secondFunction = s -> s.length() == 12 ?
                        VoidResult.success() : VoidResult.failure("failed second");

                // when
                var compositionFunction = firstFunction.flatMapToVoid(secondFunction);

                // then
                Assertions.assertThat(compositionFunction.apply(input)).isEqualTo(expectedResult);
            }

            Stream<Arguments> flatMap() {
                return Stream.of(
                        Arguments.of("Hello", VoidResult.success()),
                        Arguments.of("should fail first", VoidResult.failure("failed first")),
                        Arguments.of("2", VoidResult.failure("failed second"))
                );
            }

            @Test
            public void flatMapToVoid_function_should_not_execute_provided_bound_function_when_initial_result_is_a_failure() {
                // given
                ResultFunction<String, String, String> failure = s -> Result.failure(s + "already failed");
                Function<String, VoidResult<String>> should_not_be_executed = spiedFunction(s -> VoidResult.failure("should not be executed"));

                // when
                failure.flatMapToVoid(should_not_be_executed).apply("test");

                // then
                verify(should_not_be_executed, never()).apply(any());
            }

            @Test
            public void flatMapToVoid_supplier_should_not_accept_null_parameter() {
                assertThrows(NullPointerException.class, () ->
                        ((ResultFunction<String, String, String>) s -> null).flatMapToVoid((Supplier<VoidResult<String>>) null)
                );
            }

            @ParameterizedTest(name = "flatMapToVoid (supplier) should apply the provided bound supplier when initial result is a success")
            @MethodSource("boundSupplier")
            public void flatMapToVoid_supplier_should_apply_the_provided_bound_supplier_when_initial_result_is_a_success(
                    Supplier<VoidResult<String>> boundSupplier
            ) {
                // given
                ResultFunction<String, String, String> success = Result::success;

                // when
                var flatMappedResult = success.flatMapToVoid(boundSupplier);

                // then
                Assertions.assertThat(flatMappedResult.apply("test")).isEqualTo(boundSupplier.get());
            }

            Stream<Arguments> boundSupplier() {
                return Stream.of(
                        Arguments.of(
                                (Supplier<VoidResult<String>>) VoidResult::success
                        ),
                        Arguments.of(
                                (Supplier<VoidResult<String>>) () -> VoidResult.failure("because")
                        )
                );
            }

            @Test
            public void flatMapToVoid_supplier_should_return_the_initial_failure_when_initial_result_is_a_failure() {
                // given
                ResultFunction<String, String, String> failure = s -> Result.failure("already failed : " + s);
                Supplier<VoidResult<String>> should_not_be_executed = () -> VoidResult.failure("should not be executed");

                // when
                var flatMappedResult = failure.flatMapToVoid(should_not_be_executed);

                // then
                Assertions.assertThat(flatMappedResult.apply("because")).isEqualTo(VoidResult.failure("already failed : because"));
            }

            @Test
            public void flatMapToVoid_supplier_should_not_execute_provided_bound_supplier_when_initial_result_is_a_failure() {
                // given
                ResultFunction<String, String, String> failure = s -> Result.failure("already failed : " + s);
                Supplier<VoidResult<String>> should_not_be_executed = spiedSupplier(() -> VoidResult.failure("should not be executed"));

                // when
                failure.flatMapToVoid(should_not_be_executed).apply("test");

                // then
                verify(should_not_be_executed, never()).get();
            }

        }
    }


    @Nested
    @TestInstance(PER_CLASS)
    class Recover {

        @Test
        @DisplayName("thenRecover (Function) should not accept null parameter")
        public void thenRecover_function_should_not_accept_null_parameter() {
            assertThrows(NullPointerException.class, () ->
                    ((ResultFunction<String, Temporal, Integer>) s -> null).thenRecover((Function<Number, OffsetDateTime>) null)
            );
        }

        @Test
        @DisplayName("thenRecover (Function) should return initial success value when initial function returns a success")
        public void thenRecover_function_should_return_initial_success_value_when_initial_function_returns_a_success() {
            // given
            ResultFunction<String, String, Integer> successProvidingFunction = s -> Result.success("Success !");

            // when
            String recovered = successProvidingFunction.thenRecover(i -> String.format("failed because %d was too low", i)).apply("test");

            // then
            assertThat(recovered).isEqualTo("Success !");
        }

        @Test
        @DisplayName("thenRecover (Function) should apply the recovering function when initial function returns a failure")
        public void thenRecover_function_should_apply_the_recovering_function_when_initial_function_returns_a_failure() {
            // given
            ResultFunction<String, String, Integer> successProvidingFunction = s -> Result.failure(0);

            // when
            String recovered = successProvidingFunction.thenRecover(i -> String.format("failed because %d was too low", i)).apply("test");

            // then
            assertThat(recovered).isEqualTo("failed because 0 was too low");
        }

        @Test
        @DisplayName("thenRecover (Function) should not apply the recovering function when initial function returns a success")
        public void thenRecover_function_should_not_apply_the_recovering_function_when_initial_function_returns_a_success() {
            // given
            ResultFunction<String, String, Integer> successProvidingFunction = s -> Result.success("Success !!");
            Function<Integer, String> recoveringFunction = spiedFunction(i -> String.format("failed because %d was too low", i));

            // when
            successProvidingFunction.thenRecover(recoveringFunction).apply("test");

            // then
            verify(recoveringFunction, never()).apply(any());
        }

        @Test
        @DisplayName("thenRecover (Supplier) should not accept null parameter")
        public void thenRecover_supplier_should_not_accept_null_parameter() {
            assertThrows(NullPointerException.class, () ->
                    ((ResultFunction<String, Temporal, Integer>) s -> null).thenRecover((Supplier<Instant>) null)
            );
        }

        @Test
        @DisplayName("thenRecover (Supplier) should return initial success value when initial function returns a success")
        public void thenRecover_supplier_should_return_initial_success_value_when_initial_function_returns_a_success() {
            // given
            ResultFunction<String, String, Integer> successProvidingFunction = s -> Result.success("Success !");

            // when
            String recovered = successProvidingFunction.thenRecover(() -> "Failed !").apply("test");

            // then
            assertThat(recovered).isEqualTo("Success !");
        }

        @Test
        @DisplayName("thenRecover (Supplier) should execute the recovering supplier when initial function returns a failure")
        public void thenRecover_supplier_should_execute_the_recovering_supplier_when_initial_function_returns_a_failure() {
            // given
            ResultFunction<String, String, Integer> successProvidingFunction = s -> Result.failure(0);

            // when
            String recovered = successProvidingFunction.thenRecover(() -> "Failed !").apply("test");

            // then
            assertThat(recovered).isEqualTo("Failed !");
        }

        @Test
        @DisplayName("thenRecover (Supplier) should not execute the recovering supplier when initial function returns a success")
        public void thenRecover_supplier_should_not_execute_the_recovering_supplier_when_initial_function_returns_a_success() {
            // given
            ResultFunction<String, String, Integer> successProvidingFunction = s -> Result.success("Success !!");
            Function<Integer, String> recoveringFunction = spiedFunction(i -> String.format("failed because %d was too low", i));

            // when
            successProvidingFunction.thenRecover(recoveringFunction).apply("test");

            // then
            verify(recoveringFunction, never()).apply(any());
        }

        @Test
        @DisplayName("thenTryRecovering (Function) should not accept null parameter")
        public void thenTryRecovering_function_should_not_accept_null_parameter() {
            assertThrows(NullPointerException.class, () ->
                    ((ResultFunction<String, Temporal, Integer>) s -> null)
                            .thenTryRecovering((Function<Number, Result<OffsetDateTime, Integer>>) null)
            );
        }

        @Test
        @DisplayName("thenTryRecovering (Function) should return initial success value when initial function returns a success")
        public void thenTryRecovering_function_should_return_initial_success_value_when_initial_function_returns_a_success() {
            // given
            ResultFunction<String, String, Integer> successProvidingFunction = s -> Result.success("Success !");

            // when
            var newResult =
                    successProvidingFunction
                            .thenTryRecovering(i -> Result.success("should not matter"))
                            .apply("test");

            // then
            assertThat(newResult).isEqualTo(Result.success("Success !"));
        }

        @ParameterizedTest(name = "thenTryRecovering (Function) should return the recovering function result when initial result is a failure : {0}")
        @MethodSource("successAndFailure")
        public void thenTryRecovering_function_should_return_the_recovering_function_result_when_initial_result_is_a_failure(
                final Result<String, String> recoveringFunctionResult
        ) {
            // given
            ResultFunction<String, String, String> successProvidingFunction = s -> Result.failure("should be recovered");

            // when
            var recovered = successProvidingFunction.thenTryRecovering(i -> recoveringFunctionResult)
                    .apply("test");

            // then
            assertThat(recovered).isEqualTo(recoveringFunctionResult);
        }

        @Test
        @DisplayName("thenTryRecovering (Function) should not apply the recovering function when initial function returns a success")
        public void thenTryRecovering_function_should_not_apply_the_recovering_function_when_initial_function_returns_a_success() {
            // given
            ResultFunction<String, String, Integer> successProvidingFunction = s -> Result.success("Success !!");
            Function<Integer, Result<String, Integer>> recoveringFunction = spiedFunction(i -> Result.success("should not matter"));

            // when
            successProvidingFunction.thenTryRecovering(recoveringFunction).apply("test");

            // then
            verify(recoveringFunction, never()).apply(any());
        }

        @Test
        @DisplayName("thenTryRecovering (Supplier) should not accept null parameter")
        public void thenTryRecovering_supplier_should_not_accept_null_parameter() {
            assertThrows(NullPointerException.class, () ->
                    ((ResultFunction<String, Temporal, Integer>) s -> null)
                            .thenTryRecovering((Supplier<Result<Instant, Integer>>) null)
            );
        }

        @Test
        @DisplayName("thenTryRecovering (Supplier) should return initial success value when initial function returns a success")
        public void thenTryRecovering_supplier_should_return_initial_success_value_when_initial_function_returns_a_success() {
            // given
            ResultFunction<String, String, Integer> successProvidingFunction = s -> Result.success("Success !");

            // when
            var recovered =
                    successProvidingFunction
                            .thenTryRecovering(() -> Result.success("should not be considered"))
                            .apply("test");

            // then
            assertThat(recovered).isEqualTo(Result.success("Success !"));
        }

        @ParameterizedTest(name = "thenTryRecovering (Function) should return the recovering supplier result when initial result is a failure : {0}")
        @MethodSource("successAndFailure")
        public void thenTryRecovering_supplier_should_return_the_recovering_supplier_result_when_initial_result_is_a_failure(
                final Result<String, String> recoveringSupplierResult
        ) {
            // given
            ResultFunction<String, String, String> successProvidingFunction = s -> Result.failure("should be recovered");

            // when
            var recovered =
                    successProvidingFunction
                            .thenTryRecovering(() -> recoveringSupplierResult)
                            .apply("test");

            // then
            assertThat(recovered).isEqualTo(recoveringSupplierResult);
        }

        @Test
        @DisplayName("thenTryRecovering (Supplier) should not execute the recovering supplier when initial function returns a success")
        public void thenTryRecovering_supplier_should_not_execute_the_recovering_supplier_when_initial_function_returns_a_success() {
            // given
            ResultFunction<String, String, Integer> successProvidingFunction = s -> Result.success("Success !!");
            Function<Integer, Result<String, Integer>> recoveringFunction = spiedFunction(i -> Result.success("unused"));

            // when
            successProvidingFunction.thenTryRecovering(recoveringFunction).apply("test");

            // then
            verify(recoveringFunction, never()).apply(any());
        }

        Stream<Arguments> successAndFailure() {
            return ResultTest.successAndFailure();
        }
    }

}