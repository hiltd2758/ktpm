package com.e_health_care.web.admin.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.e_health_care.web.BvaValidationHelper;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * BVA (Boundary Value Analysis) Test cho PatientCreateDTO
 *
 * Bảng ranh giới:
 * ┌────────────┬──────────────────────────────────────────────────┐
 * │ Trường     │ Ranh giới kiểm tra                               │
 * ├────────────┼──────────────────────────────────────────────────┤
 * │ email      │ null (invalid), hợp lệ (valid), thiếu @ (invalid)│
 * │ password   │ 7 (invalid), 8 (valid), 50 (valid), 51 (invalid) │
 * │ firstName  │ "" (invalid), 1 (valid), 50 (valid), 51 (invalid)│
 * │ lastName   │ "" (invalid), 1 (valid), 50 (valid), 51 (invalid)│
 * │ phone      │ 10 (valid), 11 (invalid)                         │
 * └────────────┴──────────────────────────────────────────────────┘
 */
@Epic("Admin Management")
@Feature("Patient Create Validation")
class PatientCreateDTOBvaTest {

    // =========================================================================
    // Helper: tạo DTO hợp lệ hoàn toàn (dùng làm base, rồi override từng field)
    // =========================================================================

    private PatientCreateDTO validDto() {
        PatientCreateDTO dto = new PatientCreateDTO();
        dto.setEmail("patient@test.com");
        dto.setPassword("Password1"); // 9 ký tự – hợp lệ
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setPhone("0123456789"); // 10 ký tự – hợp lệ
        return dto;
    }

    @Test
    @Story("Nominal valid data")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra toàn bộ dữ liệu hợp lệ phải vượt qua validation")
    void allNominal_shouldBeValid() {
        PatientCreateDTO dto = validDto();
        assertTrue(BvaValidationHelper.isValid(dto),
                "DTO với tất cả field nominal phải hợp lệ");
    }

    // =========================================================================
    // PHẦN 1: EMAIL
    // =========================================================================

    /** Email = null → invalid (@NotBlank sẽ vi phạm) */
    @Test
    @Story("Email is null")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra email để trống (null) phải không hợp lệ")
    void email_null_isInvalid() {
        PatientCreateDTO dto = validDto();
        dto.setEmail(null);
        assertFalse(BvaValidationHelper.isValid(dto),
                "email=null phải không hợp lệ");
    }

    /** Email hợp lệ */
    @Test
    @Story("Valid email format")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra email đúng định dạng phải hợp lệ")
    void email_valid_isValid() {
        PatientCreateDTO dto = validDto();
        dto.setEmail("patient@test.com");
        assertTrue(BvaValidationHelper.isValid(dto),
                "email hợp lệ phải pass validation");
    }

    /** Email thiếu ký tự '@' → invalid */
    @Test
    @Story("Email missing @ symbol")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra email thiếu ký tự @ phải bị từ chối")
    void email_missingAtSign_isInvalid() {
        PatientCreateDTO dto = validDto();
        dto.setEmail("patienttest.com");
        assertFalse(BvaValidationHelper.isValid(dto),
                "email thiếu '@' phải không hợp lệ");
    }

    // =========================================================================
    // PHẦN 2: PASSWORD
    // =========================================================================

    /** Password = 7 ký tự → invalid (min = 8) */
    @Test
    @Story("Password below minimum length")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra password có 7 ký tự phải không hợp lệ")
    void password_7chars_isInvalid() {
        PatientCreateDTO dto = validDto();
        dto.setPassword("Abcd123");
        assertFalse(BvaValidationHelper.isValid(dto),
                "password 7 ký tự phải không hợp lệ (min=8)");
    }

    /** Password = 8 ký tự → valid (ranh giới dưới hợp lệ) */
    @Test
    @Story("Password at minimum boundary")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra password đúng 8 ký tự phải hợp lệ")
    void password_8chars_isValid() {
        PatientCreateDTO dto = validDto();
        dto.setPassword("Abcd1234");
        assertTrue(BvaValidationHelper.isValid(dto),
                "password 8 ký tự phải hợp lệ (boundary dưới)");
    }

    /** Password = 25 ký tự (nominal) → valid */
    @Test
    @Story("Nominal password length")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra password có độ dài tiêu chuẩn phải hợp lệ")
    void password_25chars_nominal_isValid() {
        PatientCreateDTO dto = validDto();
        dto.setPassword("A".repeat(25));
        assertTrue(BvaValidationHelper.isValid(dto),
                "password 25 ký tự (nominal) phải hợp lệ");
    }

    /** Password = 50 ký tự → valid (ranh giới trên hợp lệ) */
    @Test
    @Story("Password at maximum boundary")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra password đúng 50 ký tự phải hợp lệ")
    void password_50chars_isValid() {
        PatientCreateDTO dto = validDto();
        dto.setPassword("A".repeat(50));
        assertTrue(BvaValidationHelper.isValid(dto),
                "password 50 ký tự phải hợp lệ (boundary trên)");
    }

    /** Password = 51 ký tự → invalid (max = 50) */
    @Test
    @Story("Password exceeds maximum length")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra password vượt quá 50 ký tự phải không hợp lệ")
    void password_51chars_isInvalid() {
        PatientCreateDTO dto = validDto();
        dto.setPassword("A".repeat(51));
        assertFalse(BvaValidationHelper.isValid(dto),
                "password 51 ký tự phải không hợp lệ (max=50)");
    }

    // =========================================================================
    // PHẦN 3: FIRSTNAME
    // =========================================================================

    @Test
    @Story("First name is empty")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra firstName rỗng phải không hợp lệ")
    void firstName_empty_isInvalid() {
        PatientCreateDTO dto = validDto();
        dto.setFirstName("");
        assertFalse(BvaValidationHelper.isValid(dto),
                "firstName rỗng phải không hợp lệ");
    }

    @Test
    @Story("First name minimum boundary")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra firstName đúng 1 ký tự phải hợp lệ")
    void firstName_1char_isValid() {
        PatientCreateDTO dto = validDto();
        dto.setFirstName("A");
        assertTrue(BvaValidationHelper.isValid(dto),
                "firstName 1 ký tự phải hợp lệ (boundary dưới)");
    }

    @Test
    @Story("First name maximum boundary")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra firstName đúng 50 ký tự phải hợp lệ")
    void firstName_50chars_isValid() {
        PatientCreateDTO dto = validDto();
        dto.setFirstName("A".repeat(50));
        assertTrue(BvaValidationHelper.isValid(dto),
                "firstName 50 ký tự phải hợp lệ (boundary trên)");
    }

    @Test
    @Story("First name exceeds maximum length")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra firstName vượt quá 50 ký tự phải không hợp lệ")
    void firstName_51chars_isInvalid() {
        PatientCreateDTO dto = validDto();
        dto.setFirstName("A".repeat(51));
        assertFalse(BvaValidationHelper.isValid(dto),
                "firstName 51 ký tự phải không hợp lệ (max=50)");
    }

    // =========================================================================
    // PHẦN 4: LASTNAME
    // =========================================================================

    @Test
    @Story("Last name is empty")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra lastName rỗng phải không hợp lệ")
    void lastName_empty_isInvalid() {
        PatientCreateDTO dto = validDto();
        dto.setLastName("");
        assertFalse(BvaValidationHelper.isValid(dto),
                "lastName rỗng phải không hợp lệ");
    }

    @Test
    @Story("Last name minimum boundary")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra lastName đúng 1 ký tự phải hợp lệ")
    void lastName_1char_isValid() {
        PatientCreateDTO dto = validDto();
        dto.setLastName("B");
        assertTrue(BvaValidationHelper.isValid(dto),
                "lastName 1 ký tự phải hợp lệ (boundary dưới)");
    }

    @Test
    @Story("Last name maximum boundary")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra lastName đúng 50 ký tự phải hợp lệ")
    void lastName_50chars_isValid() {
        PatientCreateDTO dto = validDto();
        dto.setLastName("B".repeat(50));
        assertTrue(BvaValidationHelper.isValid(dto),
                "lastName 50 ký tự phải hợp lệ (boundary trên)");
    }

    @Test
    @Story("Last name exceeds maximum length")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra lastName vượt quá 50 ký tự phải không hợp lệ")
    void lastName_51chars_isInvalid() {
        PatientCreateDTO dto = validDto();
        dto.setLastName("B".repeat(51));
        assertFalse(BvaValidationHelper.isValid(dto),
                "lastName 51 ký tự phải không hợp lệ (max=50)");
    }

    // =========================================================================
    // PHẦN 5: PHONE
    // =========================================================================

    @Test
    @Story("Phone number is null")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra số điện thoại để trống phải không hợp lệ")
    void phone_null_isInvalid() {
        PatientCreateDTO dto = validDto();
        dto.setPhone(null);
        assertFalse(BvaValidationHelper.isValid(dto),
                "phone = null phải không hợp lệ");
    }

    @Test
    @Story("Phone number valid length")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra số điện thoại đúng 10 ký tự phải hợp lệ")
    void phone_10chars_isValid() {
        PatientCreateDTO dto = validDto();
        dto.setPhone("0123456789");
        assertTrue(BvaValidationHelper.isValid(dto),
                "phone 10 ký tự phải hợp lệ (max=10)");
    }

    @Test
    @Story("Phone number exceeds maximum length")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra số điện thoại vượt quá 10 ký tự phải không hợp lệ")
    void phone_11chars_isInvalid() {
        PatientCreateDTO dto = validDto();
        dto.setPhone("01234567890");
        assertFalse(BvaValidationHelper.isValid(dto),
                "phone 11 ký tự phải không hợp lệ (max=10)");
    }
}