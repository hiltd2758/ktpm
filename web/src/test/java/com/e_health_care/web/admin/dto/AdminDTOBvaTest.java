package com.e_health_care.web.admin.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class AdminDTOBvaTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        // Khởi tạo bộ kiểm tra Validation chuẩn (tương đương với việc khởi tạo BvaValidationHelper)
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // BVA TEST CHO TRƯỜNG EMAIL

    @Test
    void email_Null_Invalid() {
        AdminDTO dto = new AdminDTO();
        dto.setEmail(null);
        dto.setPassword("ValidPass123");

        Set<ConstraintViolation<AdminDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Email null phải bị báo lỗi (Invalid)");
    }

    @Test
    void email_ValidFormat_Success() {
        AdminDTO dto = new AdminDTO();
        dto.setEmail("admin@test.com"); // Valid
        dto.setPassword("ValidPass123");

        Set<ConstraintViolation<AdminDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Email đúng định dạng không được báo lỗi (Valid)");
    }

    @Test
    void email_MissingAtSymbol_Invalid() {
        AdminDTO dto = new AdminDTO();
        dto.setEmail("admintest.com"); // Thiếu @
        dto.setPassword("ValidPass123");

        Set<ConstraintViolation<AdminDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Email thiếu @ phải bị báo lỗi (Invalid)");
    }

    // BVA TEST CHO TRƯỜNG PASSWORD (Boundary: 8 -> 50)
    @Test
    void password_7Chars_Invalid() {
        AdminDTO dto = new AdminDTO();
        dto.setEmail("admin@test.com");
        dto.setPassword("1234567"); // 7 ký tự (Ngay dưới biên dưới)

        Set<ConstraintViolation<AdminDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Password 7 ký tự phải bị báo lỗi (Invalid)");
    }

    @Test
    void password_8Chars_Valid() {
        AdminDTO dto = new AdminDTO();
        dto.setEmail("admin@test.com");
        dto.setPassword("12345678"); // 8 ký tự (Ngay tại biên dưới)

        Set<ConstraintViolation<AdminDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Password 8 ký tự phải hợp lệ (Valid)");
    }

    @Test
    void password_50Chars_Valid() {
        AdminDTO dto = new AdminDTO();
        dto.setEmail("admin@test.com");
        String pass50 = "A".repeat(50); // Tạo chuỗi đúng 50 ký tự
        dto.setPassword(pass50); // 50 ký tự (Ngay tại biên trên)

        Set<ConstraintViolation<AdminDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Password 50 ký tự phải hợp lệ (Valid)");
    }

    @Test
    void password_51Chars_Invalid() {
        AdminDTO dto = new AdminDTO();
        dto.setEmail("admin@test.com");
        String pass51 = "A".repeat(51); // Tạo chuỗi 51 ký tự
        dto.setPassword(pass51); // 51 ký tự (Ngay trên biên trên)

        Set<ConstraintViolation<AdminDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Password 51 ký tự phải bị báo lỗi (Invalid)");
    }
}