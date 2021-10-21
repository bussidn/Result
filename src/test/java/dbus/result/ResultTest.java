package dbus.result;

import dbus.result.void_.VoidResult;
import dbus.result.void_.VoidResultFunction;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static dbus.result.MockitoLambdaSpying.*;
import static dbus.result.Result.failure;
import static dbus.result.Result.success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ResultTest {

    @Nested
    class Equals {

        @Test
        public void successes_should_comply_with_equals_requirements() {
            EqualsVerifier.forClass(Success.class).verify();
        }

        @Test
        public void failures_should_comply_with_equals_requirements() {
            EqualsVerifier.forClass(Failure.class).verify();
        }

    }

    @Nested
    class Construction {


        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Test
        public void success_should_not_have_null_value() {

            assertThrows(NullPointerException.class, () ->
                    success(null)
            );
        }

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
            Result<String, Integer> success = success("test");

            // when
            String matched = success.match(
                    s -> "success",
                    i -> fail("should not be executed")
            );

            // then
            assertThat(matched).isEqualTo("success");
        }

        @Test
        public void match_should_execute_failure_function_when_result_is_a_failure() {
            // given
            Result<String, Integer> failure = failure(14);

            // when
            String matched = failure.match(
                    s -> fail("should not be executed"),
                    Object::toString
            );

            // then
            assertThat(matched).isEqualTo("14");
        }

        @ParameterizedTest(name = "match should not accept null success function when result is {0}")
        @MethodSource("successAndFailure")
        public void match_should_not_accept_null_success_function(Result<String, String> result) {
            assertThrows(NullPointerException.class, () ->
                    result.match(
                            null,
                            failure -> fail("should not be executed")
                    )
            );
        }

        @ParameterizedTest(name = "match should not accept null failure function when result is {0}")
        @MethodSource("successAndFailure")
        public void match_should_not_accept_null_failure_function(Result<String, String> result) {
            assertThrows(NullPointerException.class, () ->
                    result.match(
                            success -> fail("should not be executed"),
                            null
                    )
            );
        }

        Stream<Arguments> successAndFailure() {
            return ResultTest.successAndFailure();
        }
    }

    @Nested
    @DisplayName("covariance")
    @TestInstance(PER_CLASS)
    class Covariance {

        @Test
        public void narrow_should_not_accept_null_result_input() {
            //noinspection ConstantConditions
            assertThrows(NullPointerException.class, () ->
                    Result.<String, String>narrow(null)
            );
        }

        @ParameterizedTest(name = "narrow should return an equal narrowed result when result input is not null")
        @MethodSource("subTypeResults")
        public void narrow_should_return_an_equal_narrowed_result_when_input_is_not_null(Result<?, ? extends Number> result) {
            // when
            Result<Object, Number> narrowed = Result.narrow(result);

            // then
            assertThat(narrowed).isEqualTo(result);

        }

        Stream<Arguments> subTypeResults() {
            return ResultTest.subTypeResults();
        }
    }

    static Stream<Arguments> subTypeResults() {
        return Stream.of(
                Arguments.of(Result.<Object, Number>success(new Object())),
                Arguments.of(Result.<Object, Number>failure(12)),
                Arguments.of(Result.<Object, Float>success("Object")),
                Arguments.of(Result.failure(14.2f)),
                Arguments.of(Result.<String, Number>success("Success")),
                Arguments.of(Result.<String, Number>failure(14546765L)),
                Arguments.of(Result.<String, Integer>success("not an integer")),
                Arguments.of(Result.<String, Integer>failure(42))
        );
    }

    @Nested
    @DisplayName("map functions")
    @TestInstance(PER_CLASS)
    class Functor {

        @ParameterizedTest(name = "map (Function) should not accept null parameter when result is {0}")
        @MethodSource("successAndFailure")
        public void map_function_should_not_accept_null_parameter(Result<String, String> result) {
            assertThrows(NullPointerException.class, () ->
                    result.map((Function<String, String>) null)
            );
        }

        Stream<Arguments> successAndFailure() {
            return ResultTest.successAndFailure();
        }

        @Test
        @DisplayName("map (Function) should apply mapper when result is a success")
        public void map_function_should_apply_mapper_when_result_is_a_success() {
            // given
            Result<String, Integer> success = success("Hello");

            // when
            Result<String, Integer> mapped = success.map(s -> s + " World !");

            // then
            assertThat(mapped).isEqualTo(success("Hello World !"));
        }

        @Test
        @DisplayName("map (Function) should return current failure when result is a failure")
        public void map_function_should_return_current_failure_when_result_is_a_failure() {
            // given
            Result<String, Integer> failure = failure(17);

            // when
            Result<String, Integer> mapped = failure.map(s -> s + " World !");

            // then
            assertThat(mapped).isEqualTo(failure(17));
        }

        @ParameterizedTest(name = "map (Consumer) should not accept null parameter when result is {0}")
        @MethodSource("successAndFailure")
        public void map_consumer_should_not_accept_null_parameter(Result<String, String> result) {
            assertThrows(NullPointerException.class, () ->
                    result.map((Consumer<String>) null)
            );
        }

        @Test
        @DisplayName("map (Consumer) should apply consumer when result is a success")
        public void map_consumer_should_apply_consumer_when_result_is_a_success() {
            // given
            Result<String, Integer> success = success("Hello");
            Consumer<String> spiedConsumer = spyLambda(s -> {
            }, Consumer.class);

            // when
            VoidResult<Integer> mapped = success.map(spiedConsumer);

            // then
            assertThat(mapped).isEqualTo(VoidResult.success());
            verify(spiedConsumer, once()).accept("Hello");
        }

        @Test
        @DisplayName("map (Consumer) should return current failure when result is a failure")
        public void map_consumer_should_return_current_failure_when_result_is_a_failure() {
            // given
            Result<String, Integer> failure = failure(17);
            Consumer<String> spiedConsumer = spyLambda(s -> {
            }, Consumer.class);

            // when
            VoidResult<Integer> mapped = failure.map(spiedConsumer);

            // then
            assertThat(mapped).isEqualTo(VoidResult.failure(17));
            verify(spiedConsumer, never()).accept("Hello");
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    class Monad {

        @ParameterizedTest(name = "flaMap (function) should not accept null parameter when result is {0}")
        @MethodSource("successAndFailure")
        public void flatMap_function_should_not_accept_null_parameter(Result<String, String> result) {
            assertThrows(NullPointerException.class, () ->
                    result.flatMap((Function<String, Result<Integer, String>>) null)
            );
        }

        @ParameterizedTest(name = "flatMap (function) should apply the provided bound function when initial result is a success")
        @MethodSource("boundFunction")
        public void flatMap_function_should_apply_the_provided_bound_function_when_initial_result_is_a_success(
                String initialSuccessValue,
                ResultFunction<String, Integer, String> boundFunction,
                Result<Integer, String> expectedResult
        ) {
            // given
            Result<String, String> success = Result.success(initialSuccessValue);

            // when
            Result<Integer, String> flatMappedResult = success.flatMap(boundFunction);

            // then
            assertThat(flatMappedResult).isEqualTo(expectedResult);
        }

        @Test
        public void flatMap_function_should_return_the_initial_failure_when_initial_result_is_a_failure() {
            // given
            Result<String, String> failure = Result.failure("already failed");
            Function<String, Result<Integer, String>> should_not_be_executed = s -> failure("should not be executed");

            // when
            Result<Integer, String> flatMappedResult = failure.flatMap(should_not_be_executed);

            // then
            assertThat(flatMappedResult).isEqualTo(Result.failure("already failed"));
        }

        @Test
        public void flatMap_function_should_not_execute_provided_bound_function_when_initial_result_is_a_failure() {
            // given
            Result<String, String> failure = Result.failure("already failed");
            Function<String, Result<Integer, String>> should_not_be_executed = spiedFunction(s -> failure("should not be executed"));

            // when
            failure.flatMap(should_not_be_executed);

            // then
            verify(should_not_be_executed, never()).apply(any());
        }

        @ParameterizedTest(name = "flaMap (supplier) should not accept null parameter when result is {0}")
        @MethodSource("successAndFailure")
        public void flatMap_supplier_should_not_accept_null_parameter(Result<String, String> result) {
            assertThrows(NullPointerException.class, () ->
                    result.flatMap((Supplier<Result<Integer, String>>) null)
            );
        }

        @ParameterizedTest(name = "flatMap (supplier) should apply the provided bound supplier when initial result is a success")
        @MethodSource("boundSupplier")
        public void flatMap_supplier_should_apply_the_provided_bound_supplier_when_initial_result_is_a_success(
                String initialSuccessValue,
                Supplier<Result<Integer, String>> boundSupplier,
                Result<Integer, String> expectedResult
        ) {
            // given
            Result<String, String> success = Result.success(initialSuccessValue);

            // when
            Result<Integer, String> flatMappedResult = success.flatMap(boundSupplier);

            // then
            assertThat(flatMappedResult).isEqualTo(expectedResult);
        }

        @Test
        public void flatMap_supplier_should_return_the_initial_failure_when_initial_result_is_a_failure() {
            // given
            Result<String, String> failure = Result.failure("already failed");
            Supplier<Result<Integer, String>> should_not_be_executed = () -> failure("should not be executed");

            // when
            Result<Integer, String> flatMappedResult = failure.flatMap(should_not_be_executed);

            // then
            assertThat(flatMappedResult).isEqualTo(Result.failure("already failed"));
        }

        @Test
        public void flatMap_supplier_should_not_execute_provided_bound_supplier_when_initial_result_is_a_failure() {
            // given
            Result<String, String> failure = Result.failure("already failed");
            Supplier<Result<Integer, String>> should_not_be_executed = spiedSupplier(() -> failure("should not be executed"));

            // when
            failure.flatMap(should_not_be_executed);

            // then
            verify(should_not_be_executed, never()).get();
        }

        Stream<Arguments> boundFunction() {
            return Stream.of(
                    Arguments.of("initial success",
                            (ResultFunction<String, Integer, String>) (String s) -> Result.success(s.length()),
                            Result.success(15)
                    ),
                    Arguments.of("does not matter",
                            (ResultFunction<String, Integer, String>) (String s) -> Result.failure("because"),
                            Result.failure("because"))
            );
        }

        Stream<Arguments> boundSupplier() {
            return Stream.of(
                    Arguments.of("initial success",
                            (Supplier<Result<Integer, String>>) () -> Result.success(12),
                            Result.success(12)
                    ),
                    Arguments.of("does not matter",
                            (Supplier<Result<Integer, String>>) () -> Result.failure("because"),
                            Result.failure("because"))
            );
        }

        Stream<Arguments> successAndFailure() {
            return ResultTest.successAndFailure();
        }


        @Nested
        @TestInstance(PER_CLASS)
        class ToVoidResult {

            @ParameterizedTest(name = "flaMapToVoid (function) should not accept null parameter when result is {0}")
            @MethodSource("successAndFailure")
            public void flatMapToVoid_function_should_not_accept_null_parameter(Result<String, String> result) {
                assertThrows(NullPointerException.class, () ->
                        result.flatMapToVoid((Function<String, VoidResult<String>>) null)
                );
            }

            Stream<Arguments> successAndFailure() {
                return ResultTest.successAndFailure();
            }

            @ParameterizedTest(name = "flatMapToVoid (function) should apply the provided bound function when initial result is a success")
            @MethodSource("boundFunction")
            public void flatMapToVoid_function_should_apply_the_provided_bound_function_when_initial_result_is_a_success(
                    String initialSuccessValue,
                    VoidResultFunction<String, String> boundFunction,
                    VoidResult<String> expectedResult
            ) {
                // given
                Result<String, String> success = Result.success(initialSuccessValue);

                // when
                var flatMappedResult = success.flatMapToVoid(boundFunction);

                // then
                assertThat(flatMappedResult).isEqualTo(expectedResult);
            }

            Stream<Arguments> boundFunction() {
                return Stream.of(
                        Arguments.of("initial success",
                                (VoidResultFunction<String, Integer>) (String s) -> VoidResult.success(),
                                VoidResult.success()
                        ),
                        Arguments.of("does not matter",
                                (VoidResultFunction<String, String>) (String s) -> VoidResult.failure("failed"),
                                VoidResult.failure("failed"))
                );
            }

            @Test
            public void flatMapToVoid_function_should_return_the_initial_failure_when_initial_result_is_a_failure() {
                // given
                Result<String, String> failure = Result.failure("already failed");
                Function<String, VoidResult<String>> should_not_be_executed = s -> VoidResult.failure("should not be executed");

                // when
                var flatMappedResult = failure.flatMapToVoid(should_not_be_executed);

                // then
                assertThat(flatMappedResult).isEqualTo(VoidResult.failure("already failed"));
            }

            @Test
            public void flatMapToVoid_function_should_not_execute_provided_bound_function_when_initial_result_is_a_failure() {
                // given
                Result<String, String> failure = Result.failure("already failed");
                Function<String, VoidResult<String>> should_not_be_executed = spiedFunction(s -> VoidResult.failure("should not be executed"));

                // when
                failure.flatMapToVoid(should_not_be_executed);

                // then
                verify(should_not_be_executed, never()).apply(any());
            }


            @ParameterizedTest(name = "flaMapToVoid (supplier) should not accept null parameter when result is {0}")
            @MethodSource("successAndFailure")
            public void flatMapToVoid_supplier_should_not_accept_null_parameter(Result<String, String> result) {
                assertThrows(NullPointerException.class, () ->
                        result.flatMapToVoid((Supplier<VoidResult<String>>) null)
                );
            }

            @ParameterizedTest(name = "flatMapToVoid (supplier) should apply the provided bound supplier when initial result is a success")
            @MethodSource("boundSupplier")
            public void flatMapToVoid_supplier_should_apply_the_provided_bound_supplier_when_initial_result_is_a_success(
                    String initialSuccessValue,
                    Supplier<VoidResult<String>> boundSupplier,
                    VoidResult<String> expectedResult
            ) {
                // given
                Result<String, String> success = Result.success(initialSuccessValue);

                // when
                var flatMappedResult = success.flatMapToVoid(boundSupplier);

                // then
                assertThat(flatMappedResult).isEqualTo(expectedResult);
            }

            Stream<Arguments> boundSupplier() {
                return Stream.of(
                        Arguments.of("initial success",
                                (Supplier<VoidResult<String>>) VoidResult::success,
                                VoidResult.success()
                        ),
                        Arguments.of("does not matter",
                                (Supplier<VoidResult<String>>) () -> VoidResult.failure("because"),
                                VoidResult.failure("because"))
                );
            }

            @Test
            public void flatMapToVoid_supplier_should_return_the_initial_failure_when_initial_result_is_a_failure() {
                // given
                Result<String, String> failure = Result.failure("already failed");
                Supplier<VoidResult<String>> should_not_be_executed = () -> VoidResult.failure("should not be executed");

                // when
                var flatMappedResult = failure.flatMapToVoid(should_not_be_executed);

                // then
                assertThat(flatMappedResult).isEqualTo(VoidResult.failure("already failed"));
            }

            @Test
            public void flatMapToVoid_supplier_should_not_execute_provided_bound_supplier_when_initial_result_is_a_failure() {
                // given
                Result<String, String> failure = Result.failure("already failed");
                Supplier<VoidResult<String>> should_not_be_executed = spiedSupplier(() -> VoidResult.failure("should not be executed"));

                // when
                failure.flatMapToVoid(should_not_be_executed);

                // then
                verify(should_not_be_executed, never()).get();
            }

        }
    }




    @Nested
    @TestInstance(PER_CLASS)
    class Recover {

        @ParameterizedTest(name = "recover (Function) should not accept null parameter when result is {0}")
        @MethodSource("successAndFailure")
        public void recover_function_should_not_accept_null_parameter(Result<String, String> result) {
            assertThrows(NullPointerException.class, () ->
                    result.recover((Function<String, String>) null)
            );
        }
        
        @Test
        @DisplayName("recover (Function) should return the initial success when current result is a success")
        public void recover_function_should_return_the_initial_success_when_current_result_is_a_success() {
            // given
            Result<String, String> result = Result.success("initial success");
            
            // when
            String recovered = result.recover(s -> "should not be executed");

            // then
            assertThat(recovered).isEqualTo("initial success");
        }

        @Test
        @DisplayName("recover (Function) should not execute provided recovering function when initial result is a success")
        public void recover_function_should_not_execute_provided_recovering_function_when_initial_result_is_a_success() {
            // given
            Result<String, String> result = Result.success("initial success");
            Function<String, String> should_not_be_executed = spiedFunction(s -> "should not be executed");

            // when
            result.recover(should_not_be_executed);

            // then
            verify(should_not_be_executed, never()).apply(any());
        }

        @Test
        @DisplayName("recover (Function) should return the new success when current result is a failure")
        public void recover_function_should_return_the_new_success_when_current_result_is_a_failure() {
            // given
            Result<String, String> result = Result.failure("failed so badly but it does not matter !");

            // when
            String recovered = result.recover(s -> "new success");

            // then
            assertThat(recovered).isEqualTo("new success");
        }

        @ParameterizedTest(name = "recover (Supplier) should not accept null parameter when result is {0}")
        @MethodSource("successAndFailure")
        public void recover_supplier_should_not_accept_null_parameter(Result<String, String> result) {
            assertThrows(NullPointerException.class, () ->
                    result.recover((Supplier<String>) null)
            );
        }

        @Test
        @DisplayName("recover (Supplier) should return the initial success when current result is a success")
        public void recover_supplier_should_return_the_initial_success_when_current_result_is_a_success() {
            // given
            Result<String, String> result = Result.success("initial success");

            // when
            String recovered = result.recover(() -> "should not be executed");

            // then
            assertThat(recovered).isEqualTo("initial success");
        }

        @Test
        @DisplayName("recover (Supplier) should not execute provided recovering function when initial result is a success")
        public void recover_supplier_should_not_execute_provided_recovering_function_when_initial_result_is_a_success() {
            // given
            Result<String, String> result = Result.success("initial success");
            Supplier<String> should_not_be_executed = spiedSupplier(() -> "should not be executed");

            // when
            result.recover(should_not_be_executed);

            // then
            verify(should_not_be_executed, never()).get();
        }

        @Test
        @DisplayName("recover (Supplier) should return the new success when current result is a failure")
        public void recover_supplier_should_return_the_new_success_when_current_result_is_a_failure() {
            // given
            Result<String, String> result = Result.failure("failed so badly but it does not matter !");

            // when
            String recovered = result.recover(() -> "new success");

            // then
            assertThat(recovered).isEqualTo("new success");
        }

        @ParameterizedTest(name = "tryRecovering (Function) should not accept null parameter when result is {0}")
        @MethodSource("successAndFailure")
        public void tryRecover_function_should_not_accept_null_parameter(Result<String, String> result) {
            assertThrows(NullPointerException.class, () ->
                    result.tryRecovering((Function<Object, Result<String, String>>) null)
            );
        }

        @Test
        @DisplayName("tryRecovering (Function) should return the initial success when current result is a success")
        public void tryRecovering_function_should_return_the_initial_success_when_current_result_is_a_success() {
            // given
            Result<String, String> result = Result.success("initial success");

            // when
            var recovered = result.tryRecovering(s -> Result.success("should not be executed"));

            // then
            assertThat(recovered).isEqualTo(Result.success("initial success"));
        }

        @Test
        @DisplayName("tryRecovering (Function) should not execute provided recovering function when initial result is a success")
        public void tryRecovering_function_should_not_execute_provided_recovering_function_when_initial_result_is_a_success() {
            // given
            Result<String, String> result = Result.success("initial success");
            Function<String, Result<String, String>> should_not_be_executed = spiedFunction(s -> Result.success("should not be executed"));

            // when
            result.tryRecovering(should_not_be_executed);

            // then
            verify(should_not_be_executed, never()).apply(any());
        }



        @ParameterizedTest(name = "tryRecovering (Function) should return the recovering function result when initial result is a failure : {0}")
        @MethodSource("successAndFailure")
        public void tryRecovering_function_should_return_the_new_success_when_current_result_is_a_failure(
                final Result<String, String> recoveringFunctionResult
        ) {
            // given
            Result<String, String> result = Result.failure("failed so badly but it does not matter !");

            // when
            var recovered = result.tryRecovering(s -> recoveringFunctionResult);

            // then
            assertThat(recovered).isEqualTo(recoveringFunctionResult);
        }

        @ParameterizedTest(name = "tryRecovering (Supplier) should not accept null parameter when result is {0}")
        @MethodSource("successAndFailure")
        public void tryRecovering_supplier_should_not_accept_null_parameter(Result<String, String> result) {
            assertThrows(NullPointerException.class, () ->
                    result.tryRecovering((Supplier<Result<String, String>>) null)
            );
        }

        @Test
        @DisplayName("tryRecovering (Supplier) should return the initial success when current result is a success")
        public void tryRecovering_supplier_should_return_the_initial_success_when_current_result_is_a_success() {
            // given
            Result<String, String> result = Result.success("initial success");

            // when
            var recovered = result.tryRecovering(() -> Result.success("should not be executed"));

            // then
            assertThat(recovered).isEqualTo(Result.success("initial success"));
        }

        @Test
        @DisplayName("tryRecovering (Supplier) should not execute provided recovering function when initial result is a success")
        public void tryRecovering_supplier_should_not_execute_provided_recovering_function_when_initial_result_is_a_success() {
            // given
            Result<String, String> result = Result.success("initial success");
            Supplier<Result<String, String>> should_not_be_executed = spiedSupplier(() -> Result.success("should not be executed"));

            // when
            result.tryRecovering(should_not_be_executed);

            // then
            verify(should_not_be_executed, never()).get();
        }

        @ParameterizedTest(name = "tryRecovering (Supplier) should return the recovering supplier result when initial result is a failure : {0}")
        @MethodSource("successAndFailure")
        public void tryRecovering_supplier_should_return_the_new_success_when_current_result_is_a_failure(
                final Result<String, String> recoveringSupplierResult
        ) {
            // given
            Result<String, String> result = Result.failure("failed so badly but it does not matter !");

            // when
            var recovered = result.tryRecovering(() -> recoveringSupplierResult);

            // then
            assertThat(recovered).isEqualTo(recoveringSupplierResult);
        }

        Stream<Arguments> successAndFailure() {
            return ResultTest.successAndFailure();
        }
    }

    private VerificationMode once() {
        return Mockito.times(1);
    }

    static Stream<Arguments> successAndFailure() {
        return Stream.of(
                Arguments.of(Result.<String, String>success("success")),
                Arguments.of(Result.<String, String>failure("failure"))
        );
    }

}