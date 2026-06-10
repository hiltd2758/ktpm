package com.e_health_care.web.admin.dto;

import com.e_health_care.web.BvaValidationHelper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
class PatientCreateDTOBvaTest {

    // =========================================================================
    // Helper: tạo DTO hợp lệ hoàn toàn (dùng làm base, rồi override từng field)
    // =========================================================================

    private PatientCreateDTO validDto() {
        PatientCreateDTO dto = new PatientCreateDTO();
        dto.setEmail("patient@test.com");
        dto.setPassword("Password1");    // 9 ký tự – hợp lệ
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setPhone("0123456789");      // 10 ký tự – hợp lệ
        return dto;
    }

    // =========================================================================
    // PHẦN 1: EMAIL
    // =========================================================================

    /** Email = null → invalid (@NotBlank sẽ vi phạm) */
    @Test
    void email_null_isInvalid() {
        PatientCreateDTO dto = validDto();
        dto.setEmail(null);
        assertFalse(BvaValidationHelper.isValid(dto),
                "email=null phải không hợp lệ");
    }

    /** Email hợp lệ */
    @Test
    void email_valid_isValid() {
        PatientCreateDTO dto = validDto();
        dto.setEmail("patient@test.com");
        assertTrue(BvaValidationHelper.isValid(dto),
                "email hợp lệ phải pass validation");
    }

    /** Email thiếu ký tự '@' → invalid */
    @Test
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
    void password_7chars_isInvalid() {
        PatientCreateDTO dto = validDto();
        dto.setPassword("Abcd123");   // đúng 7 ký tự
        assertFalse(BvaValidationHelper.isValid(dto),
                "password 7 ký tự phải không hợp lệ (min=8)");
    }

    /** Password = 8 ký tự → valid (ranh giới dưới hợp lệ) */
    @Test
    void password_8chars_isValid() {
        PatientCreateDTO dto = validDto();
        dto.setPassword("Abcd1234");  // đúng 8 ký tự
        assertTrue(BvaValidationHelper.isValid(dto),
                "password 8 ký tự phải hợp lệ (boundary dưới)");
    }

    /** Password = 50 ký tự → valid (ranh giới trên hợp lệ) */
    @Test
    void password_50chars_isValid() {
        PatientCreateDTO dto = validDto();
        dto.setPassword("A".repeat(50));   // đúng 50 ký tự
        assertTrue(BvaValidationHelper.isValid(dto),
                "password 50 ký tự phải hợp lệ (boundary trên)");
    }

    /** Password = 51 ký tự → invalid (max = 50) */
    @Test
    void password_51chars_isInvalid() {
        PatientCreateDTO dto = validDto();
        dto.setPassword("A".repeat(51));   // đúng 51 ký tự
        assertFalse(BvaValidationHelper.isValid(dto),
                "password 51 ký tự phải không hợp lệ (max=50)");
    }

    // =========================================================================
    // PHẦN 3: FIRSTNAME
    // =========================================================================

    /** firstName = "" (rỗng) → invalid (@NotBlank) */
    @Test
    void firstName_empty_isInvalid() {
        PatientCreateDTO dto = validDto();
        dto.setFirstName("");
        assertFalse(BvaValidationHelper.isValid(dto),
                "firstName rỗng phải không hợp lệ");
    }

    /** firstName = 1 ký tự → valid (ranh giới dưới hợp lệ) */
    @Test
    void firstName_1char_isValid() {
        PatientCreateDTO dto = validDto();
        dto.setFirstName("A");
        assertTrue(BvaValidationHelper.isValid(dto),
                "firstName 1 ký tự phải hợp lệ (boundary dưới)");
    }

    /** firstName = 50 ký tự → valid (ranh giới trên hợp lệ) */
    @Test
    void firstName_50chars_isValid() {
        PatientCreateDTO dto = validDto();
        dto.setFirstName("A".repeat(50));
        assertTrue(BvaValidationHelper.isValid(dto),
                "firstName 50 ký tự phải hợp lệ (boundary trên)");
    }

    /** firstName = 51 ký tự → invalid (max = 50) */
    @Test
    void firstName_51chars_isInvalid() {
        PatientCreateDTO dto = validDto();
        dto.setFirstName("A".repeat(51));
        assertFalse(BvaValidationHelper.isValid(dto),
                "firstName 51 ký tự phải không hợp lệ (max=50)");
    }

    // =========================================================================
    // PHẦN 4: LASTNAME
    // =========================================================================

    /** lastName = "" (rỗng) → invalid (@NotBlank) */
    @Test
    void lastName_empty_isInvalid() {
        PatientCreateDTO dto = validDto();
        dto.setLastName("");
        assertFalse(BvaValidationHelper.isValid(dto),
                "lastName rỗng phải không hợp lệ");
    }

    /** lastName = 1 ký tự → valid */
    @Test
    void lastName_1char_isValid() {
        PatientCreateDTO dto = validDto();
        dto.setLastName("B");
        assertTrue(BvaValidationHelper.isValid(dto),
                "lastName 1 ký tự phải hợp lệ (boundary dưới)");
    }

    /** lastName = 50 ký tự → valid */
    @Test
    void lastName_50chars_isValid() {
        PatientCreateDTO dto = validDto();
        dto.setLastName("B".repeat(50));
        assertTrue(BvaValidationHelper.isValid(dto),
                "lastName 50 ký tự phải hợp lệ (boundary trên)");
    }

    /** lastName = 51 ký tự → invalid */
    @Test
    void lastName_51chars_isInvalid() {
        PatientCreateDTO dto = validDto();
        dto.setLastName("B".repeat(51));
        assertFalse(BvaValidationHelper.isValid(dto),
                "lastName 51 ký tự phải không hợp lệ (max=50)");
    }

    // =========================================================================
    // PHẦN 5: PHONE
    // =========================================================================

    /** phone = 10 ký tự → valid (đúng max) */
    @Test
    void phone_10chars_isValid() {
        PatientCreateDTO dto = validDto();
        dto.setPhone("0123456789");   // đúng 10 ký tự
        assertTrue(BvaValidationHelper.isValid(dto),
                "phone 10 ký tự phải hợp lệ (max=10)");
    }

    /** phone = 11 ký tự → invalid (vượt max=10) */
    @Test
    void phone_11chars_isInvalid() {
        PatientCreateDTO dto = validDto();
        dto.setPhone("01234567890");  // 11 ký tự
        assertFalse(BvaValidationHelper.isValid(dto),
                "phone 11 ký tự phải không hợp lệ (max=10)");
    }
}
