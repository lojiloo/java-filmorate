package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class DateConstraintValidation implements ConstraintValidator<DateValidation, LocalDate> {
    private LocalDate minMoment;

    @Override
    public void initialize(DateValidation date) {
        this.minMoment = LocalDate.parse(date.value());
    }

    @Override
    public boolean isValid(LocalDate fieldValue, ConstraintValidatorContext cvc) {
        if (fieldValue == null) {
            return false;
        }
        return fieldValue.isAfter(minMoment);
    }
}
