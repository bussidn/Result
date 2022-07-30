package dbus.result.void_;

import dbus.result.Result;
import nl.jqno.equalsverifier.EqualsVerifier;
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
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class VoidResultTest {

    @Nested
    class Equals {

        @Test
        public void successes_should_comply_with_equals_requirements() {
            EqualsVerifier.forClass(dbus.result.void_.Success.class).verify();
        }

        @Test
        public void failures_should_comply_with_equals_requirements() {
            EqualsVerifier.forClass(dbus.result.void_.Failure.class).verify();
        }

    }

    @Nested
    class Construction {

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Test
        public void failure_should_not_have_null_value() {

            assertThrows(NullPointerException.class, () ->
                    failure(null)
            );
        }

    }

    @Nested
    @TestInstance(PER_CLASS)
    class UnionType {

        @Test
        public void match_should_execute_success_function_when_result_is_a_success() {
            // given
            VoidResult<Integer> success = success();

            // when
            String matched = success.match(
                    () -> "success",
                    failure -> fail("should not be executed")
            );

            // then
            assertThat(matched).isEqualTo("success");
        }

        @Test
        public void match_should_execute_failure_function_when_result_is_a_failure() {
            // given
            VoidResult<Integer> failure = failure(40);

            // when
            Integer matched = failure.match(
                    () -> fail("should not be executed"),
                    i -> i + 2
            );

            // then
            assertThat(matched).isEqualTo(42);
        }

        @ParameterizedTest(name = "match should not accept null success function when result is {0}")
        @MethodSource("successAndFailure")
        public void match_should_not_accept_null_success_function(VoidResult<String> result) {
            assertThrows(NullPointerException.class, () ->
                    result.match(
                            null,
                            failure -> fail("should not be executed")
                    )
            );
        }

        @ParameterizedTest(name = "match should not accept null failure function when result is {0}")
        @MethodSource("successAndFailure")
        public void match_should_not_accept_null_failure_function(VoidResult<String> result) {
            assertThrows(NullPointerException.class, () ->
                    result.match(
                            () -> fail("should not be executed"),
                            null
                    )
            );
        }

        Stream<Arguments> successAndFailure() {
            return VoidResultTest.successAndFailure();
        }
    }

    @Nested
    @DisplayName("covariance")
    @TestInstance(PER_CLASS)
    class Covariance {

        @Test
        public void narrow_should_not_accept_null_void_result_input() {
            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () ->
                    VoidResult.<String>narrow(null)
            );
        }

        @ParameterizedTest(name = "narrow should return an equal narrowed void result when void result input is not null")
        @MethodSource("subTypeResults")
        public void narrow_should_return_an_equal_narrowed_result_when_input_is_not_null(VoidResult<?> voidResult) {
            // when
            VoidResult<Object> narrowed = VoidResult.narrow(voidResult);

            // then
            assertThat(narrowed).isEqualTo(voidResult);

        }

        Stream<Arguments> subTypeResults() {
            return VoidResultTest.subTypeResults();
        }
    }

    static Stream<Arguments> subTypeResults() {
        return Stream.of(
                Arguments.of(VoidResult.<Number>success()),
                Arguments.of(VoidResult.<Number>failure(12)),
                Arguments.of(VoidResult.<Long>success()),
                Arguments.of(failure(14546765L)),
                Arguments.of(VoidResult.<String>success()),
                Arguments.of(failure("failure"))
        );
    }

    @Nested
    @DisplayName("map functions")
    @TestInstance(PER_CLASS)
    class Functor {


        @Nested
        @DisplayName("for success")
        @TestInstance(PER_CLASS)
        class Success {

            @ParameterizedTest(name = "map (Runnable) should not accept null parameter when result is {0}")
            @MethodSource("successAndFailure")
            public void map_runnable_should_not_accept_null_parameter(VoidResult<String> result) {
                assertThrows(NullPointerException.class, () ->
                        result.map((Runnable) null)
                );
            }

            @Test
            @DisplayName("map (Runnable) should execute runnable when result is a success")
            public void map_runnable_should_execute_runnable_when_result_is_a_success() {
                // given
                VoidResult<Integer> success = success();
                Runnable runnable = spiedRunnable();

                // when
                success.map(runnable);

                // then
                verify(runnable).run();
            }

            @Test
            @DisplayName("map (Runnable) should not execute runnable when result is a failure")
            public void map_runnable_should_not_execute_runnable_when_result_is_a_failure() {
                // given
                VoidResult<Integer> success = failure(3);
                Runnable runnable = spiedRunnable();

                // when
                success.map(runnable);

                // then
                verify(runnable, never()).run();
            }

            @Test
            @DisplayName("map (Runnable) should return a success when result is a success")
            public void map_runnable_should_return_a_success_when_result_is_a_success() {
                // given
                VoidResult<Integer> success = success();
                Runnable runnable = spiedRunnable();

                // when
                VoidResult<Integer> mapped = success.map(runnable);

                // then
                assertThat(mapped).isEqualTo(success());
            }

            @Test
            @DisplayName("map (Runnable) should execute the current failure when result is a failure")
            public void map_runnable_should_return_the_current_failure_when_result_is_a_failure() {
                // given
                VoidResult<Integer> success = failure(3);

                // when
                VoidResult<Integer> mapped = success.map(() -> {
                });

                // then
                assertThat(mapped).isEqualTo(failure(3));
            }

            @ParameterizedTest(name = "map (Supplier) should not accept null parameter when result is {0}")
            @MethodSource("successAndFailure")
            public void map_supplier_should_not_accept_null_parameter(VoidResult<String> result) {
                assertThrows(NullPointerException.class, () ->
                        result.map((Supplier<String>) null)
                );
            }

            Stream<Arguments> successAndFailure() {
                return VoidResultTest.successAndFailure();
            }

            @Test
            @DisplayName("map (Supplier) should execute supplier when result is a success")
            public void map_function_should_apply_mapper_when_result_is_a_success() {
                // given
                VoidResult<Integer> success = success();

                // when
                Result<String, Integer> mapped = success.map(() -> "mapped !");

                // then
                assertThat(mapped).isEqualTo(Result.success("mapped !"));
            }

            @Test
            @DisplayName("map (Supplier) should return current failure when result is a failure")
            public void map_function_should_return_current_failure_when_result_is_a_failure() {
                // given
                VoidResult<Integer> failure = failure(23);

                // when
                Result<String, Integer> mapped = failure.map(() -> fail("should not be executed"));

                // then
                assertThat(mapped).isEqualTo(Result.failure(23));
            }
        }


        @Nested
        @DisplayName("for failure")
        @TestInstance(PER_CLASS)
        class Failure {

            Stream<Arguments> successAndFailure() {
                return VoidResultTest.successAndFailure();
            }

            @ParameterizedTest(name = "mapFailure (Function) should not accept null parameter when result is {0}")
            @MethodSource("successAndFailure")
            public void mapFailure_function_should_not_accept_null_parameter(VoidResult<String> result) {
                assertThrows(NullPointerException.class, () ->
                        result.mapFailure((Function<String, String>) null)
                );
            }

            @Test
            @DisplayName("mapFailure (Function) should apply mapper when result is a failure")
            public void mapFailure_function_should_apply_mapper_when_result_is_a_failure() {
                // given
                VoidResult<Integer> failure = failure(3);

                // when
                VoidResult<String> mapped = failure.mapFailure((Function<? super Integer, String>) i -> Stream.generate(() -> "a").limit(i).reduce("", String::concat));

                // then
                assertThat(mapped).isEqualTo(failure("aaa"));
            }

            @Test
            @DisplayName("mapFailure (Function) should return current success when result is a success")
            public void mapFailure_function_should_return_current_success_when_result_is_a_success() {
                // given
                VoidResult<Integer> failure = success();

                // when
                VoidResult<Integer> mapped = failure.mapFailure(i -> i + 14);

                // then
                assertThat(mapped).isEqualTo(success());
            }

            @Test
            @DisplayName("mapFailure (Function) should not apply mapper when result is a success")
            public void mapFailure_function_should_not_apply_mapper_when_result_is_a_success() {
                // given
                VoidResult<Integer> success = VoidResult.success();
                Function<Integer, Integer> spiedMapper = spiedFunction(i -> i);

                // when
                success.mapFailure(spiedMapper);

                // then
                verify(spiedMapper, never()).apply(any());
            }

            @ParameterizedTest(name = "mapFailure (Supplier) should not accept null parameter when result is {0}")
            @MethodSource("successAndFailure")
            public void mapFailure_supplier_should_not_accept_null_parameter(VoidResult<String> result) {
                assertThrows(NullPointerException.class, () ->
                        result.mapFailure((Supplier<String>) null)
                );
            }

            @Test
            @DisplayName("mapFailure (Supplier) should apply mapper when result is a failure")
            public void mapFailure_supplier_should_apply_mapper_when_result_is_a_failure() {
                // given
                VoidResult<Integer> failure = failure(3);

                // when
                VoidResult<String> mapped = failure.mapFailure(() -> "created");

                // then
                assertThat(mapped).isEqualTo(failure("created"));
            }

            @Test
            @DisplayName("mapFailure (Supplier) should return current success when result is a success")
            public void mapFailure_supplier_should_return_current_success_when_result_is_a_success() {
                // given
                VoidResult<Integer> failure = success();

                // when
                VoidResult<Integer> mapped = failure.mapFailure(() -> 41);

                // then
                assertThat(mapped).isEqualTo(success());
            }

            @Test
            @DisplayName("mapFailure (Supplier) should not apply mapper when result is a success")
            public void mapFailure_supplier_should_not_apply_mapper_when_result_is_a_success() {
                // given
                VoidResult<Integer> success = success();
                Supplier<Number> spiedMapper = spiedSupplier(() -> 15.42);

                // when
                success.mapFailure(spiedMapper);

                // then
                verify(spiedMapper, never()).get();
            }

            @ParameterizedTest(name = "mapFailure (Consumer) should not accept null parameter when result is {0}")
            @MethodSource("successAndFailure")
            public void mapFailure_consumer_should_not_accept_null_parameter(VoidResult<String> result) {
                assertThrows(NullPointerException.class, () ->
                        result.mapFailure((Consumer<? super String>) null)
                );
            }

            @Test
            @DisplayName("mapFailure (Consumer) should apply mapper when result is a failure")
            public void mapFailure_consumer_should_apply_mapper_when_result_is_a_failure() {
                // given
                VoidResult<Integer> success = failure(42);
                Consumer<Number> spiedConsumer = spiedConsumer();

                // when
                success.mapFailure(spiedConsumer);

                // then
                verify(spiedConsumer, times(1)).accept(42);
            }

            @Test
            @DisplayName("mapFailure (Consumer) should not apply mapper when result is a success")
            public void mapFailure_consumer_should_not_apply_mapper_when_result_is_a_success() {
                // given
                VoidResult<Integer> success = success();
                Consumer<Number> spiedConsumer = spiedConsumer();

                // when
                success.mapFailure(spiedConsumer);

                // then
                verify(spiedConsumer, never()).accept(any());
            }
        }

    }

    @Nested
    @TestInstance(PER_CLASS)
    class Monad {

        @ParameterizedTest(name = "flaMap should not accept null parameter when result is {0}")
        @MethodSource("successAndFailure")
        public void flatMap_supplier_should_not_accept_null_parameter(VoidResult<String> result) {
            assertThrows(NullPointerException.class, () ->
                    result.flatMap(null)
            );
        }

        @ParameterizedTest(name = "flatMap should apply the provided bound supplier when initial result is a success")
        @MethodSource("boundVoidResultSupplier")
        public void flatMap_should_apply_the_provided_bound_supplier_when_initial_result_is_a_success(
                Supplier<VoidResult<String>> boundVoidResultSupplier,
                VoidResult<String> expectedResult
        ) {
            // given
            VoidResult<String> success = success();

            // when
            VoidResult<String> flatMappedResult = success.flatMap(boundVoidResultSupplier);

            // then
            assertThat(flatMappedResult).isEqualTo(expectedResult);
        }

        Stream<Arguments> boundVoidResultSupplier() {
            return Stream.of(
                    Arguments.of(
                            (Supplier<VoidResult<String>>) VoidResult::success,
                            success()
                    ),
                    Arguments.of(
                            (Supplier<VoidResult<String>>) () -> failure("because"),
                            failure("because")
                    )
            );
        }

        @Test
        public void flatMap_should_return_the_initial_failure_when_initial_result_is_a_failure() {
            // given
            VoidResult<String> failure = failure("already failed");
            Supplier<VoidResult<String>> should_not_be_executed = () -> failure("should not be executed");

            // when
            VoidResult<String> flatMappedResult = failure.flatMap(should_not_be_executed);

            // then
            assertThat(flatMappedResult).isEqualTo(failure("already failed"));
        }

        @Test
        public void flatMap_should_not_execute_provided_bound_supplier_when_initial_result_is_a_failure() {
            // given
            VoidResult<String> failure = failure("already failed");
            Supplier<VoidResult<String>> should_not_be_executed = spiedSupplier(() -> failure("should not be executed"));

            // when
            failure.flatMap(should_not_be_executed);

            // then
            verify(should_not_be_executed, never()).get();
        }

        Stream<Arguments> successAndFailure() {
            return VoidResultTest.successAndFailure();
        }

        @Nested
        @TestInstance(PER_CLASS)
        class ToResult {

            @ParameterizedTest(name = "flatMapToResult should not accept null parameter when result is {0}")
            @MethodSource("successAndFailure")
            public void flatMapToResult_supplier_should_not_accept_null_parameter(VoidResult<String> result) {
                assertThrows(NullPointerException.class, () ->
                        result.flatMapToResult((Supplier<Result<Integer, String>>) null)
                );
            }

            @ParameterizedTest(name = "flatMapToResult should apply the provided bound supplier when initial result is a success")
            @MethodSource("boundSupplier")
            public void flatMapToResult_should_apply_the_provided_bound_supplier_when_initial_result_is_a_success(
                    Supplier<Result<Integer, String>> boundSupplier,
                    Result<Integer, String> expectedResult
            ) {
                // given
                VoidResult<String> success = success();

                // when
                Result<Integer, String> flatMappedResult = success.flatMapToResult(boundSupplier);

                // then
                assertThat(flatMappedResult).isEqualTo(expectedResult);
            }

            Stream<Arguments> boundSupplier() {
                return Stream.of(
                        Arguments.of(
                                (Supplier<Result<Integer, String>>) () -> Result.success(12),
                                Result.success(12)
                        ),
                        Arguments.of(
                                (Supplier<Result<Integer, String>>) () -> Result.failure("because"),
                                Result.failure("because")
                        )
                );
            }

            @Test
            public void flatMapToResult_should_return_the_initial_failure_when_initial_result_is_a_failure() {
                // given
                VoidResult<String> failure = failure("already failed");
                Supplier<Result<Integer, String>> should_not_be_executed = () -> Result.failure("should not be executed");

                // when
                Result<Integer, String> flatMappedResult = failure.flatMapToResult(should_not_be_executed);

                // then
                assertThat(flatMappedResult).isEqualTo(Result.failure("already failed"));
            }

            @Test
            public void flatMapToResult_should_not_execute_provided_bound_supplier_when_initial_result_is_a_failure() {
                // given
                VoidResult<String> failure = failure("already failed");
                Supplier<Result<Integer, String>> should_not_be_executed = spiedSupplier(() -> Result.failure("should not be executed"));

                // when
                failure.flatMapToResult(should_not_be_executed);

                // then
                verify(should_not_be_executed, never()).get();
            }

            Stream<Arguments> successAndFailure() {
                return VoidResultTest.successAndFailure();
            }

        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    class Recover {

        @ParameterizedTest(name = "tryRecovering (Function) should not accept null parameter when result is {0}")
        @MethodSource("successAndFailure")
        public void tryRecovering_function_should_not_accept_null_parameter(VoidResult<String> result) {
            assertThrows(NullPointerException.class, () ->
                    result.tryRecovering((Function<Object, VoidResult<String>>) null)
            );
        }

        @Test
        @DisplayName("tryRecovering (Function) should return the initial success when current result is a success")
        public void tryRecovering_function_should_return_the_initial_success_when_current_result_is_a_success() {
            // given
            VoidResult<String> result = success();

            // when
            var recovered = result.tryRecovering(s -> failure("should not be executed"));

            // then
            assertThat(recovered).isEqualTo(success());
        }

        @Test
        @DisplayName("tryRecovering (Function) should not execute provided recovering function when initial result is a success")
        public void tryRecovering_function_should_not_execute_provided_recovering_function_when_initial_result_is_a_success() {
            // given
            VoidResult<String> result = success();
            Function<String, VoidResult<String>> should_not_be_executed = spiedFunction(s -> success());

            // when
            result.tryRecovering(should_not_be_executed);

            // then
            verify(should_not_be_executed, never()).apply(any());
        }


        @ParameterizedTest(name = "tryRecovering (Function) should return the recovering function result when initial result is a failure : {0}")
        @MethodSource("successAndFailure")
        public void tryRecovering_function_should_return_the_new_success_when_current_result_is_a_failure(
                final VoidResult<String> recoveringFunctionResult
        ) {
            // given
            VoidResult<String> result = failure("failed so badly but it does not matter !");

            // when
            var recovered = result.tryRecovering(s -> recoveringFunctionResult);

            // then
            assertThat(recovered).isEqualTo(recoveringFunctionResult);
        }

        @ParameterizedTest(name = "tryRecovering (Supplier) should not accept null parameter when result is {0}")
        @MethodSource("successAndFailure")
        public void tryRecovering_supplier_should_not_accept_null_parameter(VoidResult<String> result) {
            assertThrows(NullPointerException.class, () ->
                    result.tryRecovering((Supplier<VoidResult<String>>) null)
            );
        }

        @Test
        @DisplayName("tryRecovering (Supplier) should return the initial success when current result is a success")
        public void tryRecovering_supplier_should_return_the_initial_success_when_current_result_is_a_success() {
            // given
            VoidResult<String> result = success();

            // when
            var recovered = result.tryRecovering(() -> failure("should not be executed"));

            // then
            assertThat(recovered).isEqualTo(success());
        }

        @Test
        @DisplayName("tryRecovering (Supplier) should not execute provided recovering function when initial result is a success")
        public void tryRecovering_supplier_should_not_execute_provided_recovering_function_when_initial_result_is_a_success() {
            // given
            VoidResult<String> result = success();
            Supplier<VoidResult<String>> should_not_be_executed = spiedSupplier(() -> failure("should not be executed"));

            // when
            result.tryRecovering(should_not_be_executed);

            // then
            verify(should_not_be_executed, never()).get();
        }

        @ParameterizedTest(name = "tryRecovering (Supplier) should return the recovering supplier result when initial result is a failure : {0}")
        @MethodSource("successAndFailure")
        public void tryRecovering_supplier_should_return_the_new_success_when_current_result_is_a_failure(
                final VoidResult<String> recoveringSupplierResult
        ) {
            // given
            VoidResult<String> result = failure("failed so badly but it does not matter !");

            // when
            var recovered = result.tryRecovering(() -> recoveringSupplierResult);

            // then
            assertThat(recovered).isEqualTo(recoveringSupplierResult);
        }

        Stream<Arguments> successAndFailure() {
            return VoidResultTest.successAndFailure();
        }
    }

    static Stream<Arguments> successAndFailure() {
        return Stream.of(
                Arguments.of(VoidResult.<String>success()),
                Arguments.of(failure("failure"))
        );
    }

}