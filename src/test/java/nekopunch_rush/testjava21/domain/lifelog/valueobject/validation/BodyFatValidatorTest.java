package nekopunch_rush.testjava21.domain.lifelog.valueobject.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BodyFatValidatorTest {

    private final BodyFatValidator validator = new BodyFatValidator();

    @Test
    void testValidValues() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid(0.0, null));
        assertTrue(validator.isValid(50.0, null));
        assertTrue(validator.isValid(100.0, null));
    }

    @Test
    void testInvalidValues() {
        assertFalse(validator.isValid(-0.1, null));
        assertFalse(validator.isValid(100.1, null));
        assertFalse(validator.isValid(Double.NEGATIVE_INFINITY, null));
        assertFalse(validator.isValid(Double.POSITIVE_INFINITY, null));
        assertFalse(validator.isValid(Double.NaN, null));
    }
}
