package dbus;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                    Result.success(null)
            );
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Test
        public void failure_should_not_have_null_value() {

            assertThrows(NullPointerException.class, () ->
                    Result.failure(null)
            );
        }

    }

    @Nested
    class UnionType {

        @Test
        public void match_should_execute_success_function_when_result_is_a_success() {
            // given
            Result<String, Integer> success = Result.success("test");

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
            Result<String, Integer> success = Result.failure(14);

            // when
            String matched = success.match(
                    s -> fail("should not be executed"),
                    failure -> "42"
            );

            // then
            assertThat(matched).isEqualTo("42");
        }
    }

}