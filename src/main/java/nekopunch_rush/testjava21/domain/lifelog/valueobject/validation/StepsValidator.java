package nekopunch_rush.testjava21.domain.lifelog.valueobject.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StepsValidator implements ConstraintValidator<ValidSteps, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value == null || (value >= 0 && value <= 100000);
    }
}

