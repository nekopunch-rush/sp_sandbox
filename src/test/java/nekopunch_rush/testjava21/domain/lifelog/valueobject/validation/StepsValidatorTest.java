package nekopunch_rush.testjava21.domain.lifelog.valueobject.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StepsValidatorTest {

    private final StepsValidator validator = new StepsValidator();

    @Test
    void testValidValues() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid(0, null));
        assertTrue(validator.isValid(50000, null));
        assertTrue(validator.isValid(100000, null));
    }

    @Test
    void testInvalidValues() {
        assertFalse(validator.isValid(-1, null));
        assertFalse(validator.isValid(100001, null));
        assertFalse(validator.isValid(Integer.MIN_VALUE, null));
        assertFalse(validator.isValid(Integer.MAX_VALUE, null));
    }
}
