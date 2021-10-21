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

import static dbus.result.MockitoLambdaSpying.spiedFunction;
import static dbus.result.MockitoLambdaSpying.spiedSupplier;
import static dbus.result.Result.success;
import static dbus.result.void_.VoidResult.failure;
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
        public void map_function_should_be_provided_with_non_null_mapper() {
            // given
            ResultFunction<String, String, String> resultFunction = Result::success;

            // then
            assertThrows(NullPointerException.class, () ->
                    resultFunction.map((Function<String, String>) null)
            );
        }

        @Test
        public void map_function_should_be_compose_the_provided_mapper() {
            // given
            ResultFunction<String, String, String> resultFunction = Result::success;
            Function<String, Integer> mapper = String::length;

            // when
            var composed = resultFunction.map(mapper);

            // then
            assertThat(composed.apply("should be 12")).isEqualTo(success(12));
        }

        @Test
        public void map_consumer_should_be_provided_with_non_null_consumer() {
            // given
            ResultFunction<String, String, String> resultFunction = Result::success;

            // then
            assertThrows(NullPointerException.class, () ->
                    resultFunction.map((Consumer<String>) null)
            );
        }

        @Test
        public void map_consumer_should_be_compose_the_provided_mapper() {
            // given
            ResultFunction<String, String, String> resultFunction = Result::failure;
            Consumer<String> consumer = s -> {
            };

            // when
            var composed = resultFunction.map(consumer);

            // then
            assertThat(composed.apply("because")).isEqualTo(failure("because"));
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
        public void thenRecover_function_should_not_accept_null_parameter()  {
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
            String recovered = successProvidingFunction.thenRecover(recoveringFunction).apply("test");

            // then
            verify(recoveringFunction, never()).apply(any());
        }

        @Test
        @DisplayName("thenRecover (Supplier) should not accept null parameter")
        public void thenRecover_supplier_should_not_accept_null_parameter()  {
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
            String recovered = successProvidingFunction.thenRecover(recoveringFunction).apply("test");

            // then
            verify(recoveringFunction, never()).apply(any());
        }
    }

}