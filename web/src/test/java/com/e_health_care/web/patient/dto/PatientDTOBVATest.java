package com.e_health_care.web.patient.dto;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

// Fix: @Epic/@Feature dùng để nhóm test theo nghiệp vụ ở tab "Behaviors"
@Epic("Patient Management")
@Feature("Patient DTO Validation")
class PatientDTOBVATest {

    private PatientDTO patientDTO;

    @BeforeEach
    void setUp() {
        patientDTO = new PatientDTO();
    }

    // ══════ NHÓM 1: EMAIL ══════

    @Test
    @DisplayName("BVA_EMAIL_01 – Email null → getEmail() = null")
    @Story("Kiểm tra trường Email")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra DTO chấp nhận gán email null và getEmail() trả về đúng giá trị null.")
    void email_null() {
        patientDTO.setEmail(null);
        assertNull(patientDTO.getEmail());
    }

    @Test
    @DisplayName("BVA_EMAIL_04 – Email hợp lệ (patient@test.com)")
    @Story("Kiểm tra trường Email")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra DTO lưu và trả về đúng địa chỉ email hợp lệ có định dạng chuẩn (có @, có domain).")
    void email_minimalValid() {
        patientDTO.setEmail("patient@test.com");
        assertEquals("patient@test.com", patientDTO.getEmail());
    }

    @Test
    @DisplayName("BVA_EMAIL_05 – Email thiếu @ (invalid)")
    @Story("Kiểm tra trường Email")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra DTO lưu được email sai định dạng (thiếu @) ở tầng setter — validate định dạng phải do lớp service/annotation đảm nhiệm.")
    void email_missingAt() {
        patientDTO.setEmail("patienttest.com");
        assertEquals("patienttest.com", patientDTO.getEmail());
    }

    @Test
    @DisplayName("BVA_EMAIL_06 – Email 255 ký tự (vượt biên trên)")
    @Story("Kiểm tra trường Email")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra giá trị biên trên: DTO chấp nhận lưu email 255 ký tự ở tầng setter, giới hạn độ dài được enforce bởi annotation validate.")
    void email_255chars() {
        String email = "a".repeat(64) + "@" + "b".repeat(186) + ".com";
        assertEquals(255, email.length());
        patientDTO.setEmail(email);
        assertEquals(email, patientDTO.getEmail());
    }

    // ══════ NHÓM 2: PASSWORD ══════

    @Test
    @DisplayName("BVA_PASS_01 – Password null")
    @Story("Kiểm tra trường Password")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra DTO chấp nhận gán password null và getPassword() trả về đúng giá trị null.")
    void password_null() {
        patientDTO.setPassword(null);
        assertNull(patientDTO.getPassword());
    }

    @Test
    @DisplayName("BVA_PASS_02 – Password rỗng")
    @Story("Kiểm tra trường Password")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra DTO lưu và trả về chuỗi rỗng khi gán password rỗng — validate độ dài do annotation đảm nhiệm.")
    void password_empty() {
        patientDTO.setPassword("");
        assertEquals("", patientDTO.getPassword());
    }

    @Test
    @DisplayName("BVA_PASS_03 – Password 1 ký tự")
    @Story("Kiểm tra trường Password")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra giá trị biên: DTO lưu password 1 ký tự, nằm dưới ngưỡng min 8 ký tự.")
    void password_oneChar() {
        patientDTO.setPassword("a");
        assertEquals(1, patientDTO.getPassword().length());
    }

    @Test
    @DisplayName("BVA_PASS_04 – Password 7 ký tự (dưới min 8)")
    @Story("Kiểm tra trường Password")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra giá trị biên dưới ngoài: password 7 ký tự nằm dưới ngưỡng tối thiểu 8 ký tự, sẽ bị annotation @Size từ chối khi validate.")
    void password_sevenChars_belowMin() {
        patientDTO.setPassword("1234567");
        assertEquals(7, patientDTO.getPassword().length());
    }

    @Test
    @DisplayName("BVA_PASS_05 – Password đúng 8 ký tự (biên min)")
    @Story("Kiểm tra trường Password")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra giá trị biên dưới trong: password đúng 8 ký tự là giá trị hợp lệ nhỏ nhất, annotation @Size phải chấp nhận.")
    void password_eightChars_atMin() {
        patientDTO.setPassword("12345678");
        assertEquals(8, patientDTO.getPassword().length());
    }

    @Test
    @DisplayName("BVA_PASS_06 – Password 50 ký tự (valid)")
    @Story("Kiểm tra trường Password")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra password 50 ký tự nằm trong khoảng hợp lệ [8..128], DTO lưu và trả về đúng độ dài.")
    void password_50chars_valid() {
        String pass = "A".repeat(50);
        patientDTO.setPassword(pass);
        assertEquals(50, patientDTO.getPassword().length());
    }

    @Test
    @DisplayName("BVA_PASS_07 – Password 51 ký tự (vượt max)")
    @Story("Kiểm tra trường Password")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra giá trị biên trên ngoài (nếu max=50): password 51 ký tự, annotation @Size phải từ chối khi validate.")
    void password_51chars_overMax() {
        String pass = "A".repeat(51);
        patientDTO.setPassword(pass);
        assertEquals(51, patientDTO.getPassword().length());
    }

    @Test
    @DisplayName("BVA_PASS_128 – Password 128 ký tự (biên max)")
    @Story("Kiểm tra trường Password")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra giá trị biên trên trong: password đúng 128 ký tự là giá trị hợp lệ lớn nhất, annotation @Size phải chấp nhận.")
    void password_128chars_atMax() {
        String pass = "A".repeat(128);
        patientDTO.setPassword(pass);
        assertEquals(128, patientDTO.getPassword().length());
    }

    @Test
    @DisplayName("BVA_PASS_129 – Password 129 ký tự (vượt biên max)")
    @Story("Kiểm tra trường Password")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra giá trị biên trên ngoài: password 129 ký tự vượt ngưỡng tối đa 128 ký tự, annotation @Size phải từ chối khi validate.")
    void password_129chars_overMax() {
        String pass = "A".repeat(129);
        patientDTO.setPassword(pass);
        assertEquals(129, patientDTO.getPassword().length());
    }

    // ══════ NHÓM 3: PHONE ══════

    @Test
    @DisplayName("BVA_PHONE_01 – Phone null")
    @Story("Kiểm tra trường Phone")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra DTO chấp nhận gán phone null và getPhone() trả về đúng giá trị null.")
    void phone_null() {
        patientDTO.setPhone(null);
        assertNull(patientDTO.getPhone());
    }

    @Test
    @DisplayName("BVA_PHONE_02 – Phone rỗng")
    @Story("Kiểm tra trường Phone")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra DTO lưu và trả về chuỗi rỗng khi gán phone rỗng — validate độ dài do annotation đảm nhiệm.")
    void phone_empty() {
        patientDTO.setPhone("");
        assertEquals("", patientDTO.getPhone());
    }

    @Test
    @DisplayName("BVA_PHONE_03 – Phone 9 số (dưới min)")
    @Story("Kiểm tra trường Phone")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra giá trị biên dưới ngoài: phone 9 số nằm dưới ngưỡng tối thiểu 10 số, annotation @Size phải từ chối khi validate.")
    void phone_nineDigits_belowMin() {
        patientDTO.setPhone("012345678");
        assertEquals(9, patientDTO.getPhone().length());
    }

    @Test
    @DisplayName("BVA_PHONE_04 – Phone 10 số (valid)")
    @Story("Kiểm tra trường Phone")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra giá trị biên dưới trong: phone 10 số là giá trị hợp lệ nhỏ nhất, annotation @Size phải chấp nhận.")
    void phone_tenDigits_atMin() {
        patientDTO.setPhone("0987654321");
        assertEquals(10, patientDTO.getPhone().length());
    }

    @Test
    @DisplayName("BVA_PHONE_05 – Phone 11 số (invalid)")
    @Story("Kiểm tra trường Phone")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra giá trị biên trên ngoài: phone 11 số vượt ngưỡng tối đa 10 số, annotation @Size phải từ chối khi validate.")
    void phone_elevenDigits_overMax() {
        patientDTO.setPhone("09876543210");
        assertEquals(11, patientDTO.getPhone().length());
    }

    // ══════ NHÓM 4: FIRST NAME ══════

    @Test
    @DisplayName("BVA_NAME_01 – firstName null")
    @Story("Kiểm tra trường First Name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra DTO chấp nhận gán firstName null và getFirstName() trả về đúng giá trị null.")
    void firstName_null() {
        patientDTO.setFirstName(null);
        assertNull(patientDTO.getFirstName());
    }

    @Test
    @DisplayName("BVA_NAME_02 – firstName rỗng (invalid)")
    @Story("Kiểm tra trường First Name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra DTO lưu được firstName rỗng ở tầng setter — annotation @NotBlank phải từ chối khi validate.")
    void firstName_empty() {
        patientDTO.setFirstName("");
        assertEquals("", patientDTO.getFirstName());
    }

    @Test
    @DisplayName("BVA_NAME_03 – firstName 1 ký tự (biên min)")
    @Story("Kiểm tra trường First Name")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra giá trị biên dưới trong: firstName 1 ký tự là giá trị hợp lệ nhỏ nhất, annotation @Size phải chấp nhận.")
    void firstName_oneChar_atMin() {
        patientDTO.setFirstName("A");
        assertEquals("A", patientDTO.getFirstName());
    }

    @Test
    @DisplayName("BVA_NAME_04 – firstName 50 ký tự (biên max)")
    @Story("Kiểm tra trường First Name")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra giá trị biên trên trong: firstName đúng 50 ký tự là giá trị hợp lệ lớn nhất, annotation @Size phải chấp nhận.")
    void firstName_50Chars_atMax() {
        String name = "A".repeat(50);
        patientDTO.setFirstName(name);
        assertEquals(50, patientDTO.getFirstName().length());
    }

    @Test
    @DisplayName("BVA_NAME_05 – firstName 51 ký tự (vượt biên max)")
    @Story("Kiểm tra trường First Name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra giá trị biên trên ngoài: firstName 51 ký tự vượt ngưỡng tối đa 50 ký tự, annotation @Size phải từ chối khi validate.")
    void firstName_51Chars_overMax() {
        String name = "A".repeat(51);
        patientDTO.setFirstName(name);
        assertEquals(51, patientDTO.getFirstName().length());
    }

    // ══════ NHÓM 5: LAST NAME ══════

    @Test
    @DisplayName("BVA_NAME_06 – lastName null")
    @Story("Kiểm tra trường Last Name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra DTO chấp nhận gán lastName null và getLastName() trả về đúng giá trị null.")
    void lastName_null() {
        patientDTO.setLastName(null);
        assertNull(patientDTO.getLastName());
    }

    @Test
    @DisplayName("BVA_NAME_06 – lastName rỗng (invalid)")
    @Story("Kiểm tra trường Last Name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra DTO lưu được lastName rỗng ở tầng setter — annotation @NotBlank phải từ chối khi validate.")
    void lastName_empty() {
        patientDTO.setLastName("");
        assertEquals("", patientDTO.getLastName());
    }

    @Test
    @DisplayName("BVA_NAME_07 – lastName 1 ký tự (biên min)")
    @Story("Kiểm tra trường Last Name")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra giá trị biên dưới trong: lastName 1 ký tự là giá trị hợp lệ nhỏ nhất, annotation @Size phải chấp nhận.")
    void lastName_oneChar_atMin() {
        patientDTO.setLastName("B");
        assertEquals("B", patientDTO.getLastName());
    }

    @Test
    @DisplayName("BVA_NAME_08 – lastName 50 ký tự (biên max)")
    @Story("Kiểm tra trường Last Name")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra giá trị biên trên trong: lastName đúng 50 ký tự là giá trị hợp lệ lớn nhất, annotation @Size phải chấp nhận.")
    void lastName_50Chars_atMax() {
        String name = "A".repeat(50);
        patientDTO.setLastName(name);
        assertEquals(50, patientDTO.getLastName().length());
    }

    @Test
    @DisplayName("BVA_NAME_09 – lastName 51 ký tự (vượt biên max)")
    @Story("Kiểm tra trường Last Name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra giá trị biên trên ngoài: lastName 51 ký tự vượt ngưỡng tối đa 50 ký tự, annotation @Size phải từ chối khi validate.")
    void lastName_51Chars_overMax() {
        String name = "A".repeat(51);
        patientDTO.setLastName(name);
        assertEquals(51, patientDTO.getLastName().length());
    }

    // ══════ NHÓM 6: DATE OF BIRTH ══════

    @Test
    @DisplayName("BVA_DOB_01 – dateOfBirth null")
    @Story("Kiểm tra trường Date of Birth")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra DTO chấp nhận gán dateOfBirth null và getDateOfBirth() trả về đúng giá trị null.")
    void dateOfBirth_null() {
        patientDTO.setDateOfBirth(null);
        assertNull(patientDTO.getDateOfBirth());
    }

    @Test
    @DisplayName("BVA_DOB_02 – dateOfBirth hôm nay")
    @Story("Kiểm tra trường Date of Birth")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra DTO lưu và trả về đúng ngày sinh là ngày hôm nay (LocalDate.now()).")
    void dateOfBirth_today() {
        LocalDate today = LocalDate.now();
        patientDTO.setDateOfBirth(today);
        assertEquals(today, patientDTO.getDateOfBirth());
    }

    @Test
    @DisplayName("BVA_DOB_03 – dateOfBirth ngày mai (tương lai)")
    @Story("Kiểm tra trường Date of Birth")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra DTO lưu được ngày sinh trong tương lai ở tầng setter — validate ngày hợp lệ do annotation @Past đảm nhiệm.")
    void dateOfBirth_tomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        patientDTO.setDateOfBirth(tomorrow);
        assertTrue(patientDTO.getDateOfBirth().isAfter(LocalDate.now()));
    }

    @Test
    @DisplayName("BVA_DOB_04 – dateOfBirth quá khứ hợp lệ")
    @Story("Kiểm tra trường Date of Birth")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra DTO lưu và trả về đúng ngày sinh trong quá khứ hợp lệ (01/01/1990).")
    void dateOfBirth_pastDate_valid() {
        LocalDate dob = LocalDate.of(1990, 1, 1);
        patientDTO.setDateOfBirth(dob);
        assertEquals(LocalDate.of(1990, 1, 1), patientDTO.getDateOfBirth());
    }

    @Test
    @DisplayName("BVA_DOB_05 – dateOfBirth LocalDate.MIN")
    @Story("Kiểm tra trường Date of Birth")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra giá trị biên cực tiểu: DTO lưu và trả về đúng LocalDate.MIN mà không gây lỗi ngoại lệ.")
    void dateOfBirth_minDate() {
        patientDTO.setDateOfBirth(LocalDate.MIN);
        assertEquals(LocalDate.MIN, patientDTO.getDateOfBirth());
    }

    // ══════ NHÓM 7: ROLE ══════

    @Test
    @DisplayName("BVA_ROLE_01 – role mặc định → \"PATIENT\"")
    @Story("Kiểm tra trường Role")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra giá trị mặc định của role trả về 'PATIENT' khi chưa gán giá trị, đảm bảo bệnh nhân mới đăng ký luôn có role đúng.")
    void role_default_returnsPatient() {
        assertEquals("PATIENT", patientDTO.getUppercase_role());
    }

    @Test
    @DisplayName("BVA_ROLE_02 – role = \"admin\" → \"ADMIN\"")
    @Story("Kiểm tra trường Role")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra hàm getUppercase_role() chuyển đổi đúng giá trị role sang chữ hoa (admin → ADMIN).")
    void role_customValue_uppercase() {
        patientDTO.setRole("admin");
        assertEquals("ADMIN", patientDTO.getUppercase_role());
    }

    @Test
    @DisplayName("BVA_ROLE_03 – role rỗng → \"\"")
    @Story("Kiểm tra trường Role")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra hàm getUppercase_role() trả về chuỗi rỗng khi role được gán là chuỗi rỗng, không gây lỗi ngoại lệ.")
    void role_empty_uppercase() {
        patientDTO.setRole("");
        assertEquals("", patientDTO.getUppercase_role());
    }

    @Test
    @DisplayName("BVA_ROLE_04 – role null → NullPointerException")
    @Story("Kiểm tra trường Role")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra hàm getUppercase_role() ném NullPointerException khi role được gán null, xác nhận cần guard null trước khi gọi toUpperCase().")
    void role_null_throwsNPE() {
        patientDTO.setRole(null);
        assertThrows(NullPointerException.class, () -> patientDTO.getUppercase_role());
    }

    // ══════ NHÓM 8: ID ══════

    @Test
    @DisplayName("BVA_ID_01 – id = 0")
    @Story("Kiểm tra trường ID")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra DTO lưu và trả về đúng giá trị id = 0 (biên dưới).")
    void id_zero() {
        patientDTO.setId(0L);
        assertEquals(0L, patientDTO.getId());
    }

    @Test
    @DisplayName("BVA_ID_02 – id = 1")
    @Story("Kiểm tra trường ID")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra DTO lưu và trả về đúng giá trị id = 1, là giá trị ID hợp lệ nhỏ nhất trong thực tế (auto-increment).")
    void id_one() {
        patientDTO.setId(1L);
        assertEquals(1L, patientDTO.getId());
    }

    @Test
    @DisplayName("BVA_ID_03 – id = Long.MAX_VALUE")
    @Story("Kiểm tra trường ID")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra giá trị biên cực đại: DTO lưu và trả về đúng Long.MAX_VALUE mà không bị tràn số (overflow).")
    void id_maxLong() {
        patientDTO.setId(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, patientDTO.getId());
    }

    @Test
    @DisplayName("BVA_ID_04 – id âm")
    @Story("Kiểm tra trường ID")
    @Severity(SeverityLevel.MINOR)
    @Description("Kiểm tra DTO chấp nhận gán id âm ở tầng setter — validate id hợp lệ (> 0) do lớp service đảm nhiệm.")
    void id_negative() {
        patientDTO.setId(-1L);
        assertEquals(-1L, patientDTO.getId());
    }

    // ══════ BVA VALIDATION THỰC SỰ (4n+1) ══════

    @Test
    @Story("Kiểm tra toàn bộ trường hợp hợp lệ (nominal)")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra DTO hợp lệ khi toàn bộ các trường đều có giá trị đúng chuẩn — đây là baseline để so sánh với các case lỗi từng trường.")
    void allNominal_shouldBeValid() {
        assertTrue(isValid(validDto()));
    }

    @Test
    @Story("Kiểm tra trường Email")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra annotation @Email từ chối DTO khi email sai định dạng (thiếu @), trong khi các trường còn lại đều hợp lệ.")
    void email_invalid_shouldFail() {
        PatientDTO dto = validDto();
        dto.setEmail("patienttest.com");
        assertFalse(isValid(dto));
    }

    @Test
    @Story("Kiểm tra trường Email")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra annotation @NotNull/@NotBlank từ chối DTO khi email là null, trong khi các trường còn lại đều hợp lệ.")
    void email_null_shouldFail() {
        PatientDTO dto = validDto();
        dto.setEmail(null);
        assertFalse(isValid(dto));
    }

    @Test
    @Story("Kiểm tra trường Password")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra annotation @Size từ chối DTO khi password 7 ký tự (dưới min 8), trong khi các trường còn lại đều hợp lệ.")
    void password_7chars_shouldFail() {
        PatientDTO dto = validDto();
        dto.setPassword("1234567");
        assertFalse(isValid(dto));
    }

    @Test
    @Story("Kiểm tra trường Password")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra annotation @Size chấp nhận DTO khi password đúng 8 ký tự (biên min hợp lệ), trong khi các trường còn lại đều hợp lệ.")
    void password_8chars_shouldPass() {
        PatientDTO dto = validDto();
        dto.setPassword("12345678");
        assertTrue(isValid(dto));
    }

    @Test
    @Story("Kiểm tra trường Password")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra annotation @Size từ chối DTO khi password 51 ký tự vượt ngưỡng max, trong khi các trường còn lại đều hợp lệ.")
    void password_51chars_shouldFail() {
        PatientDTO dto = validDto();
        dto.setPassword("A".repeat(51));
        assertFalse(isValid(dto));
    }

    @Test
    @Story("Kiểm tra trường First Name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra annotation @NotBlank từ chối DTO khi firstName rỗng, trong khi các trường còn lại đều hợp lệ.")
    void firstName_empty_shouldFail() {
        PatientDTO dto = validDto();
        dto.setFirstName("");
        assertFalse(isValid(dto));
    }

    @Test
    @Story("Kiểm tra trường Last Name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra annotation @NotBlank từ chối DTO khi lastName rỗng, trong khi các trường còn lại đều hợp lệ.")
    void lastName_empty_shouldFail() {
        PatientDTO dto = validDto();
        dto.setLastName("");
        assertFalse(isValid(dto));
    }

    @Test
    @Story("Kiểm tra trường Last Name")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra annotation @Size từ chối DTO khi lastName 51 ký tự vượt ngưỡng max 50, trong khi các trường còn lại đều hợp lệ.")
    void lastName_51chars_shouldFail() {
        PatientDTO dto = validDto();
        dto.setLastName("A".repeat(51));
        assertFalse(isValid(dto));
    }

    @Test
    @Story("Kiểm tra trường Phone")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra annotation @Size từ chối DTO khi phone 11 số vượt ngưỡng max 10, trong khi các trường còn lại đều hợp lệ.")
    void phone_11chars_shouldFail() {
        PatientDTO dto = validDto();
        dto.setPhone("09876543210");
        assertFalse(isValid(dto));
    }

    // ══════ VALIDATION HELPER ══════

    private jakarta.validation.Validator getValidator() {
        return jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
    }

    private boolean isValid(PatientDTO dto) {
        return getValidator().validate(dto).isEmpty();
    }

    private PatientDTO validDto() {
        PatientDTO dto = new PatientDTO();
        dto.setEmail("patient@test.com");
        dto.setPassword("A".repeat(25));
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setPhone("0909123456");
        return dto;
    }
}