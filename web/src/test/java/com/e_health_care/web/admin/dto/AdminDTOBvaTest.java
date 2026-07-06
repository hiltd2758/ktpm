package com.e_health_care.web.admin.dto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@Epic("Admin Management")
@Feature("Admin DTO Validation")
public class AdminDTOBvaTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        // Khởi tạo bộ kiểm tra Validation chuẩn (tương đương với việc khởi tạo BvaValidationHelper)
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==========================
    // BVA TEST CHO TRƯỜNG EMAIL
    // ==========================

    @Test
    @Story("Email is null")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra email không được để trống, nếu null thì phải phát sinh lỗi validation")
    void email_Null_Invalid() {
        AdminDTO dto = new AdminDTO();
        dto.setEmail(null);
        dto.setPassword("ValidPass123");

        Set<ConstraintViolation<AdminDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Email null phải bị báo lỗi (Invalid)");
    }

    @Test
    @Story("Valid email format")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra email đúng định dạng phải vượt qua validation")
    void email_ValidFormat_Success() {
        AdminDTO dto = new AdminDTO();
        dto.setEmail("admin@test.com");
        dto.setPassword("ValidPass123");

        Set<ConstraintViolation<AdminDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Email đúng định dạng không được báo lỗi (Valid)");
    }

    @Test
    @Story("Email missing @ symbol")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra email thiếu ký tự @ phải bị từ chối")
    void email_MissingAtSymbol_Invalid() {
        AdminDTO dto = new AdminDTO();
        dto.setEmail("admintest.com");
        dto.setPassword("ValidPass123");

        Set<ConstraintViolation<AdminDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Email thiếu @ phải bị báo lỗi (Invalid)");
    }

    // ================================
    // BVA TEST CHO TRƯỜNG PASSWORD
    // Boundary: 8 -> 50
    // ================================

    @Test
    @Story("Password below minimum length")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra password có 7 ký tự phải bị báo lỗi vì nhỏ hơn giới hạn tối thiểu")
    void password_7Chars_Invalid() {
        AdminDTO dto = new AdminDTO();
        dto.setEmail("admin@test.com");
        dto.setPassword("1234567");

        Set<ConstraintViolation<AdminDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Password 7 ký tự phải bị báo lỗi (Invalid)");
    }

    @Test
    @Story("Password at minimum boundary")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra password đúng 8 ký tự phải hợp lệ")
    void password_8Chars_Valid() {
        AdminDTO dto = new AdminDTO();
        dto.setEmail("admin@test.com");
        dto.setPassword("12345678");

        Set<ConstraintViolation<AdminDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Password 8 ký tự phải hợp lệ (Valid)");
    }

    @Test
    @Story("Password at maximum boundary")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra password đúng 50 ký tự phải hợp lệ")
    void password_50Chars_Valid() {
        AdminDTO dto = new AdminDTO();
        dto.setEmail("admin@test.com");
        String pass50 = "A".repeat(50);
        dto.setPassword(pass50);

        Set<ConstraintViolation<AdminDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Password 50 ký tự phải hợp lệ (Valid)");
    }

    @Test
    @Story("Password exceeds maximum length")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra password dài hơn 50 ký tự phải bị từ chối")
    void password_51Chars_Invalid() {
        AdminDTO dto = new AdminDTO();
        dto.setEmail("admin@test.com");
        String pass51 = "A".repeat(51);
        dto.setPassword(pass51);

        Set<ConstraintViolation<AdminDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Password 51 ký tự phải bị báo lỗi (Invalid)");
    }

    @Test
    @Story("Nominal valid data")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra trường hợp dữ liệu hợp lệ hoàn toàn phải vượt qua toàn bộ validation")
    void allNominal_shouldBeValid() {
        AdminDTO dto = new AdminDTO();

        dto.setEmail("admin@test.com");
        dto.setPassword("A".repeat(25));

        Set<ConstraintViolation<AdminDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(),
                "Trường hợp Nominal (giá trị an toàn nhất) phải hoàn toàn hợp lệ");
    }
}