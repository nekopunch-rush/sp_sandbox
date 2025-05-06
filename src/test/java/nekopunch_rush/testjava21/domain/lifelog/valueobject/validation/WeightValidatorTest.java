package nekopunch_rush.testjava21.domain.lifelog.valueobject.validation;

import nekopunch_rush.testjava21.domain.lifelog.valueobject.Weight;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeightValidatorTest {

    private final WeightValidator validator = new WeightValidator();

//    private Weight mockWeight(double value) {
//        return new Weight() {
//            @Override
//            public double getValue() {
//                return value;
//            }
//        };
//    }

    @Test
    void testValidValues() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid(Weight.of(0.1), null));
        assertTrue(validator.isValid(Weight.of(70.0), null));
        assertTrue(validator.isValid(Weight.of(500.0), null));
    }

    @Test
    void testInvalidValues() {
        assertFalse(validator.isValid(Weight.of(0.0), null));
        assertFalse(validator.isValid(Weight.of(-1.0), null));
        assertFalse(validator.isValid(Weight.of(500.1), null));
        assertFalse(validator.isValid(Weight.of(Double.NEGATIVE_INFINITY), null));
        assertFalse(validator.isValid(Weight.of(Double.POSITIVE_INFINITY), null));
        assertFalse(validator.isValid(Weight.of(Double.NaN), null));
    }
}
