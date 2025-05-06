package nekopunch_rush.testjava21.domain.lifelog.valueobject.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CaloriesBurnedValidatorTest {

    private final CaloriesBurnedValidator validator = new CaloriesBurnedValidator();

    @Test
    void testValidValues() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid(0, null));
        assertTrue(validator.isValid(5000, null));
        assertTrue(validator.isValid(10000, null));
    }

    @Test
    void testInvalidValues() {
        assertFalse(validator.isValid(-1, null));
        assertFalse(validator.isValid(10001, null));
        assertFalse(validator.isValid(Integer.MIN_VALUE, null));
        assertFalse(validator.isValid(Integer.MAX_VALUE, null));
    }
}
