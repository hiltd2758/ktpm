package com.e_health_care.web.admin.dto;

import com.e_health_care.web.BvaValidationHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUpdateDTOBvaTest {

    @Test
    void newPassword_null_shouldBeInvalid() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO(null);
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    void newPassword_empty_shouldBeInvalid() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO("");
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    void newPassword_7chars_minMinus1_shouldBeInvalid() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO("1234567");
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    void newPassword_8chars_min_shouldBeValid() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO("12345678");
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    void newPassword_25chars_nominal_shouldBeValid() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO("a".repeat(25));
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    void newPassword_50chars_max_shouldBeValid() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO("a".repeat(50));
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    void newPassword_51chars_maxPlus1_shouldBeInvalid() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO("a".repeat(51));
        assertFalse(BvaValidationHelper.isValid(dto));
    }
}