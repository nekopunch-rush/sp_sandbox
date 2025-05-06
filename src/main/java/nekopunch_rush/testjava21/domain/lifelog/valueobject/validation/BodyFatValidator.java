package nekopunch_rush.testjava21.domain.lifelog.valueobject.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BodyFatValidator implements ConstraintValidator<ValidBodyFat, Double> {

    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        return value == null || (value >= 0 && value <= 100);
    }
}
