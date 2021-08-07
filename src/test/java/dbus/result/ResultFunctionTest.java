package dbus.result;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResultFunctionTest {

    @Test
    public void asResultFunction_should_not_accept_null_input() {
        //noinspection ResultOfMethodCallIgnored
        assertThrows(NullPointerException.class, () ->
                ResultFunction.<Integer, String, Object>asResultFunction(null));
    }

    @Test
    public void asResultFunction_should_convert_a_returning_result_function_to_a_result_function() {
        // given
        Function<Number, Result<String, String>> f = n -> Result.success(n.toString());

        // when
        ResultFunction<Integer, Object, Object> converted = ResultFunction.asResultFunction(f);

        // then
        assertThat(converted.apply(12 )).isEqualTo(f.apply(12));
    }

}