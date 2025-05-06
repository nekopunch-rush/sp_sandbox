package nekopunch_rush.testjava21.domain.lifelog.valueobject.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = WeightValidator.class)
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidWeight {
    String message() default "体重は0より大きく500以下である必要があります";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
