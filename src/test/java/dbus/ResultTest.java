package dbus;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static dbus.Result.failure;
import static dbus.Result.success;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

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
                    failure -> fail("should not be executed")
            );

            // then
            assertThat(matched).isEqualTo("success");
        }

        @Test
        public void match_should_execute_failure_function_when_result_is_a_failure() {
            // given
            Result<String, Integer> success = failure(14);

            // when
            String matched = success.match(
                    s -> fail("should not be executed"),
                    failure -> "42"
            );

            // then
            assertThat(matched).isEqualTo("42");
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
    @DisplayName("map should")
    @TestInstance(PER_CLASS)
    class Functor {

        @ParameterizedTest(name = "map should not accept null parameter when result is {0}")
        @MethodSource("successAndFailure")
        public void map_should_not_accept_null_parameter(Result<String, String> result) {
            assertThrows(NullPointerException.class, () ->
                    result.map(null)
            );
        }

        Stream<Arguments> successAndFailure() {
            return ResultTest.successAndFailure();
        }

        @Test
        public void map_should_apply_mapper_when_result_is_a_success() {
            // given
            Result<String, Integer> success = success("Hello");

            // when
            Result<String, Integer> mapped = success.map(s -> s + " World !");

            // then
            assertThat(mapped).isEqualTo(success("Hello World !"));
        }

        @Test
        public void map_should_return_current_failure_when_result_is_a_failure() {
            // given
            Result<String, Integer> failure = failure(17);

            // when
            Result<String, Integer> mapped = failure.map(s -> s + " World !");

            // then
            assertThat(mapped).isEqualTo(failure(17));
        }
    }

    static Stream<Arguments> successAndFailure() {
        return Stream.of(
                Arguments.of(Result.<String, String>success("success")),
                Arguments.of(Result.<String, String>failure("failure"))
        );
    }

}