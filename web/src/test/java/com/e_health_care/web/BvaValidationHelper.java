package com.e_health_care.web;

import jakarta.validation.*;
import java.util.Set;

public class BvaValidationHelper {
    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public static <T> Set<ConstraintViolation<T>> validate(T dto) {
        return validator.validate(dto);
    }

    public static <T> boolean isValid(T dto) {
        return validator.validate(dto).isEmpty();
    }
}