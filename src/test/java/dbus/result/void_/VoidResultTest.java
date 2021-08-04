package dbus.result.void_;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static dbus.result.void_.VoidResult.failure;
import static dbus.result.void_.VoidResult.success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

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
            VoidResult<Integer> success = failure(14);

            // when
            String matched = success.match(
                    () -> fail("should not be executed"),
                    failure -> "42"
            );

            // then
            assertThat(matched).isEqualTo("42");
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

    static Stream<Arguments> successAndFailure() {
        return Stream.of(
                Arguments.of(VoidResult.<String>success()),
                Arguments.of(VoidResult.failure("failure"))
        );
    }

}