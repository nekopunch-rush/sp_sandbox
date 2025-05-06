package nekopunch_rush.testjava21.domain.lifelog.valueobject.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = MuscleMassValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMuscleMass {
    String message() default "筋肉量は0〜100の範囲である必要があります。";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
