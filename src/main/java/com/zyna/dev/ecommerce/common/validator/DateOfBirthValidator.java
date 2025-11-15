package com.zyna.dev.ecommerce.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class DateOfBirthValidator implements ConstraintValidator<DateOfBirthConstraint, LocalDate> {

    private int minAge;
    private int maxAge;

    @Override
    public void initialize(DateOfBirthConstraint constraintAnnotation) {
        this.minAge = constraintAnnotation.min();
        this.maxAge = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        LocalDate today = LocalDate.now();

        if (value.isAfter(today)) {
            return false;
        }

        int age = Period.between(value, today).getYears();

        return age >= minAge && age <= maxAge;
    }
}
