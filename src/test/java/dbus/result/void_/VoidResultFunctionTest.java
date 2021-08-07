package dbus.result.void_;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VoidResultFunctionTest {

    @Test
    public void asVoidResultFunction_should_not_accept_null_input() {
        //noinspection ResultOfMethodCallIgnored
        assertThrows(NullPointerException.class, () ->
                VoidResultFunction.<Integer, Object>asVoidResultFunction(null));
    }

    @Test
    public void asVoidResultFunction_should_convert_a_returning_void_result_function_to_a_void_result_function() {
        // given
        Function<Number, VoidResult<String>> f = n -> VoidResult.failure(n.toString());

        // when
        VoidResultFunction<Integer, Object> converted = VoidResultFunction.asVoidResultFunction(f);

        // then
        assertThat(converted.apply(72 )).isEqualTo(f.apply(72));
    }

}