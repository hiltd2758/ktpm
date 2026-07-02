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

@Epic("Admin Management")
@Feature("Password Update Validation")
class PasswordUpdateDTOBvaTest {

    @Test
    @Story("Password is null")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra mật khẩu mới không được để trống (null)")
    void newPassword_null_shouldBeInvalid() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO(null);
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    @Story("Password is empty")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra mật khẩu mới rỗng phải không hợp lệ")
    void newPassword_empty_shouldBeInvalid() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO("");
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    @Story("Password below minimum length")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra mật khẩu mới có 7 ký tự phải bị từ chối vì nhỏ hơn giới hạn tối thiểu")
    void newPassword_7chars_minMinus1_shouldBeInvalid() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO("1234567");
        assertFalse(BvaValidationHelper.isValid(dto));
    }

    @Test
    @Story("Password at minimum boundary")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra mật khẩu mới đúng 8 ký tự phải hợp lệ")
    void newPassword_8chars_min_shouldBeValid() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO("12345678");
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    @Story("Nominal password length")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra mật khẩu mới có độ dài tiêu chuẩn phải hợp lệ")
    void newPassword_25chars_nominal_shouldBeValid() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO("a".repeat(25));
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    @Story("Password at maximum boundary")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra mật khẩu mới đúng 50 ký tự phải hợp lệ")
    void newPassword_50chars_max_shouldBeValid() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO("a".repeat(50));
        assertTrue(BvaValidationHelper.isValid(dto));
    }

    @Test
    @Story("Password exceeds maximum length")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra mật khẩu mới vượt quá 50 ký tự phải không hợp lệ")
    void newPassword_51chars_maxPlus1_shouldBeInvalid() {
        PasswordUpdateDTO dto = new PasswordUpdateDTO("a".repeat(51));
        assertFalse(BvaValidationHelper.isValid(dto));
    }
}