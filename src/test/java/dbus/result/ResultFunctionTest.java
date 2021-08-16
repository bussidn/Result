package dbus.result;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.function.Function;

import static dbus.result.Result.success;
import static dbus.result.void_.VoidResult.failure;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            Consumer<String> consumer = s -> {};

            // when
            var composed = resultFunction.map(consumer);

            // then
            assertThat(composed.apply("because")).isEqualTo(failure("because"));
        }
    }

}