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

import java.util.function.Supplier;
import java.util.stream.Stream;

import static dbus.result.MockitoLambdaSpying.spiedRunnable;
import static dbus.result.void_.VoidResult.failure;
import static dbus.result.void_.VoidResult.success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.never;
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
                Arguments.of(VoidResult.failure(14546765L)),
                Arguments.of(VoidResult.<String>success()),
                Arguments.of(VoidResult.failure("failure"))
        );
    }

    @Nested
    @DisplayName("map functions")
    @TestInstance(PER_CLASS)
    class Functor {

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
            VoidResult<Integer> mapped = success.map(() -> {});

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

    static Stream<Arguments> successAndFailure() {
        return Stream.of(
                Arguments.of(VoidResult.<String>success()),
                Arguments.of(VoidResult.failure("failure"))
        );
    }

}