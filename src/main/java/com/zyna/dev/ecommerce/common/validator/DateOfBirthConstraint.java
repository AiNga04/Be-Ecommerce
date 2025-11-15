package com.zyna.dev.ecommerce.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {DateOfBirthValidator.class})
public @interface DateOfBirthConstraint {

    String message() default "Age must be between {min} and {max}";

    // min age
    int min() default 12;

    // max age
    int max() default 100;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
