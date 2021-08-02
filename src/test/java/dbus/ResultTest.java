package dbus;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ResultTest {

    @Test
    public void successes_should_comply_with_equals_requirements() {
        EqualsVerifier.forClass(Success.class).verify();
    }

    @Test
    public void failures_should_comply_with_equals_requirements() {
        EqualsVerifier.forClass(Failure.class).verify();
    }

}