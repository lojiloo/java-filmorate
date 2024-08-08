package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = DateConstraintValidation.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DateValidation {
    String value() default "1895-12-28";

    String message() default "Date must not be before {value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
