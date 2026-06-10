package com.e_health_care.web.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.e_health_care.web.doctor.dto.DoctorDTO;

public final class BvaValidationHelper {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private BvaValidationHelper() {
    }

    public static boolean isValidDoctorDto(DoctorDTO doctorDTO) {
        return validateDoctorDto(doctorDTO).isEmpty();
    }

    public static List<String> validateDoctorDto(DoctorDTO doctorDTO) {
        List<String> errors = new ArrayList<>();
        if (doctorDTO == null) {
            errors.add("doctorDTO");
            return errors;
        }

        validateEmail(doctorDTO.getEmail(), errors);
        validatePassword(doctorDTO.getPassword(), errors);
        validateFirstName(doctorDTO.getFirstName(), errors);
        validatePhone(doctorDTO.getPhone(), errors);

        return errors;
    }

    private static void validateEmail(String email, List<String> errors) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("email");
        }
    }

    private static void validatePassword(String password, List<String> errors) {
        if (password == null || password.length() < 8 || password.length() > 50) {
            errors.add("password");
        }
    }

    private static void validateFirstName(String firstName, List<String> errors) {
        if (firstName == null || firstName.isBlank() || firstName.length() > 50) {
            errors.add("firstName");
        }
    }

    private static void validatePhone(String phone, List<String> errors) {
        if (phone == null || phone.length() != 10) {
            errors.add("phone");
        }
    }
}