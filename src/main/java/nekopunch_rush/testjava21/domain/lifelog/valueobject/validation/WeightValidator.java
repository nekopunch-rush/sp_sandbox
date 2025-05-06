package nekopunch_rush.testjava21.domain.lifelog.valueobject.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.Weight;

public class WeightValidator implements ConstraintValidator<ValidWeight, Weight> {

    @Override
    public boolean isValid(Weight value, ConstraintValidatorContext context) {
        if (value == null) return true; // nullチェックは別でやる想定

        double w = value.getValue();
        return w > 0 && w <= 500;
    }
}
