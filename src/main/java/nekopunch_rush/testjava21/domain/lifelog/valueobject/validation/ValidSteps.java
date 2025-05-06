package nekopunch_rush.testjava21.domain.lifelog.valueobject.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = StepsValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSteps {
    String message() default "歩数は0〜100000の範囲である必要があります。";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

