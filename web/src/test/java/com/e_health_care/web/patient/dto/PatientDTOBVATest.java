package com.e_health_care.web.patient.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PatientDTOBVATest {

    private PatientDTO patientDTO;

    @BeforeEach
    void setUp() {
        patientDTO = new PatientDTO();
    }

    // ══════ NHÓM 1: EMAIL ══════

    @Test
    @DisplayName("BVA_EMAIL_01 – Email null → getEmail() = null")
    void email_null() {
        patientDTO.setEmail(null);
        assertNull(patientDTO.getEmail());
    }

//    @Test
//    @DisplayName("BVA_EMAIL_02 – Email rỗng")
//    void email_empty() {
//        patientDTO.setEmail("");
//        assertEquals("", patientDTO.getEmail());
//    }

//    @Test
//    @DisplayName("BVA_EMAIL_03 – Email 1 ký tự")
//    void email_oneChar() {
//        patientDTO.setEmail("a");
//        assertEquals("a", patientDTO.getEmail());
//    }

    @Test
    @DisplayName("BVA_EMAIL_04 – Email hợp lệ (patient@test.com)")
    void email_minimalValid() {
        patientDTO.setEmail("patient@test.com");
        assertEquals("patient@test.com", patientDTO.getEmail());
    }

    @Test
    @DisplayName("BVA_EMAIL_05 – Email thiếu @ (invalid)")
    void email_missingAt() {
        patientDTO.setEmail("patienttest.com");
        assertEquals("patienttest.com", patientDTO.getEmail());
    }

//    @Test
//    @DisplayName("BVA_EMAIL_06 – Email 255 ký tự (vượt biên trên)")
//    void email_255chars() {
//        String email = "a".repeat(64) + "@" + "b".repeat(186) + ".com";
//        assertEquals(255, email.length());
//        patientDTO.setEmail(email);
//        assertEquals(email, patientDTO.getEmail());
//    }

    // ══════ NHÓM 2: PASSWORD ══════

//    @Test
//    @DisplayName("BVA_PASS_01 – Password null")
//    void password_null() {
//        patientDTO.setPassword(null);
//        assertNull(patientDTO.getPassword());
//    }

//    @Test
//    @DisplayName("BVA_PASS_02 – Password rỗng")
//    void password_empty() {
//        patientDTO.setPassword("");
//        assertEquals("", patientDTO.getPassword());
//    }

//    @Test
//    @DisplayName("BVA_PASS_03 – Password 1 ký tự")
//    void password_oneChar() {
//        patientDTO.setPassword("a");
//        assertEquals(1, patientDTO.getPassword().length());
//    }

    @Test
    @DisplayName("BVA_PASS_04 – Password 7 ký tự (dưới min 8)")
    void password_sevenChars_belowMin() {
        patientDTO.setPassword("1234567");
        assertEquals(7, patientDTO.getPassword().length());
    }

    @Test
    @DisplayName("BVA_PASS_05 – Password đúng 8 ký tự (biên min)")
    void password_eightChars_atMin() {
        patientDTO.setPassword("12345678");
        assertEquals(8, patientDTO.getPassword().length());
    }

    @Test
    @DisplayName("BVA_PASS_06 – Password 50 ký tự (valid)")
    void password_50chars_valid() {
        String pass = "A".repeat(50);
        patientDTO.setPassword(pass);
        assertEquals(50, patientDTO.getPassword().length());
    }

    @Test
    @DisplayName("BVA_PASS_07 – Password 51 ký tự (vượt max)")
    void password_51chars_overMax() {
        String pass = "A".repeat(51);
        patientDTO.setPassword(pass);
        assertEquals(51, patientDTO.getPassword().length());
    }

//    @Test
//    @DisplayName("BVA_PASS_128 – Password 128 ký tự (biên max)")
//    void password_128chars_atMax() {
//        String pass = "A".repeat(128);
//        patientDTO.setPassword(pass);
//        assertEquals(128, patientDTO.getPassword().length());
//    }

//    @Test
//    @DisplayName("BVA_PASS_129 – Password 129 ký tự (vượt biên max)")
//    void password_129chars_overMax() {
//        String pass = "A".repeat(129);
//        patientDTO.setPassword(pass);
//        assertEquals(129, patientDTO.getPassword().length());
//    }

    // ══════ NHÓM 3: PHONE ══════

//    @Test
//    @DisplayName("BVA_PHONE_01 – Phone null")
//    void phone_null() {
//        patientDTO.setPhone(null);
//        assertNull(patientDTO.getPhone());
//    }

//    @Test
//    @DisplayName("BVA_PHONE_02 – Phone rỗng")
//    void phone_empty() {
//        patientDTO.setPhone("");
//        assertEquals("", patientDTO.getPhone());
//    }

//    @Test
//    @DisplayName("BVA_PHONE_03 – Phone 9 số (dưới min)")
//    void phone_nineDigits_belowMin() {
//        patientDTO.setPhone("012345678");
//        assertEquals(9, patientDTO.getPhone().length());
//    }

    @Test
    @DisplayName("BVA_PHONE_04 – Phone 10 số (valid)")
    void phone_tenDigits_atMin() {
        patientDTO.setPhone("0987654321");
        assertEquals(10, patientDTO.getPhone().length());
    }

    @Test
    @DisplayName("BVA_PHONE_05 – Phone 11 số (invalid)")
    void phone_elevenDigits_overMax() {
        patientDTO.setPhone("09876543210");
        assertEquals(11, patientDTO.getPhone().length());
    }

    // ══════ NHÓM 4: FIRST NAME ══════

//    @Test
//    @DisplayName("BVA_NAME_01 – firstName null")
//    void firstName_null() {
//        patientDTO.setFirstName(null);
//        assertNull(patientDTO.getFirstName());
//    }

    @Test
    @DisplayName("BVA_NAME_02 – firstName rỗng (invalid)")
    void firstName_empty() {
        patientDTO.setFirstName("");
        assertEquals("", patientDTO.getFirstName());
    }

    @Test
    @DisplayName("BVA_NAME_03 – firstName 1 ký tự (biên min)")
    void firstName_oneChar_atMin() {
        patientDTO.setFirstName("A");
        assertEquals("A", patientDTO.getFirstName());
    }

    @Test
    @DisplayName("BVA_NAME_04 – firstName 50 ký tự (biên max)")
    void firstName_50Chars_atMax() {
        String name = "A".repeat(50);
        patientDTO.setFirstName(name);
        assertEquals(50, patientDTO.getFirstName().length());
    }

    @Test
    @DisplayName("BVA_NAME_05 – firstName 51 ký tự (vượt biên max)")
    void firstName_51Chars_overMax() {
        String name = "A".repeat(51);
        patientDTO.setFirstName(name);
        assertEquals(51, patientDTO.getFirstName().length());
    }

    // ══════ NHÓM 5: LAST NAME ══════

//    @Test
//    @DisplayName("BVA_NAME_06 – lastName null")
//    void lastName_null() {
//        patientDTO.setLastName(null);
//        assertNull(patientDTO.getLastName());
//    }

    @Test
    @DisplayName("BVA_NAME_06 – lastName rỗng (invalid)")
    void lastName_empty() {
        patientDTO.setLastName("");
        assertEquals("", patientDTO.getLastName());
    }

    @Test
    @DisplayName("BVA_NAME_07 – lastName 1 ký tự (biên min)")
    void lastName_oneChar_atMin() {
        patientDTO.setLastName("B");
        assertEquals("B", patientDTO.getLastName());
    }

    @Test
    @DisplayName("BVA_NAME_08 – lastName 50 ký tự (biên max)")
    void lastName_50Chars_atMax() {
        String name = "A".repeat(50);
        patientDTO.setLastName(name);
        assertEquals(50, patientDTO.getLastName().length());
    }

    @Test
    @DisplayName("BVA_NAME_09 – lastName 51 ký tự (vượt biên max)")
    void lastName_51Chars_overMax() {
        String name = "A".repeat(51);
        patientDTO.setLastName(name);
        assertEquals(51, patientDTO.getLastName().length());
    }

    // ══════ COMMENT OUT – NGOÀI YÊU CẦU ══════

//    // NHÓM: DATE OF BIRTH
//    @Test
//    @DisplayName("BVA_DOB_01 – dateOfBirth null")
//    void dateOfBirth_null() {
//        patientDTO.setDateOfBirth(null);
//        assertNull(patientDTO.getDateOfBirth());
//    }
//
//    @Test
//    @DisplayName("BVA_DOB_02 – dateOfBirth hôm nay")
//    void dateOfBirth_today() {
//        LocalDate today = LocalDate.now();
//        patientDTO.setDateOfBirth(today);
//        assertEquals(today, patientDTO.getDateOfBirth());
//    }
//
//    @Test
//    @DisplayName("BVA_DOB_03 – dateOfBirth ngày mai (tương lai)")
//    void dateOfBirth_tomorrow() {
//        LocalDate tomorrow = LocalDate.now().plusDays(1);
//        patientDTO.setDateOfBirth(tomorrow);
//        assertTrue(patientDTO.getDateOfBirth().isAfter(LocalDate.now()));
//    }
//
//    @Test
//    @DisplayName("BVA_DOB_04 – dateOfBirth quá khứ hợp lệ")
//    void dateOfBirth_pastDate_valid() {
//        LocalDate dob = LocalDate.of(1990, 1, 1);
//        patientDTO.setDateOfBirth(dob);
//        assertEquals(LocalDate.of(1990, 1, 1), patientDTO.getDateOfBirth());
//    }
//
//    @Test
//    @DisplayName("BVA_DOB_05 – dateOfBirth LocalDate.MIN")
//    void dateOfBirth_minDate() {
//        patientDTO.setDateOfBirth(LocalDate.MIN);
//        assertEquals(LocalDate.MIN, patientDTO.getDateOfBirth());
//    }

//    // NHÓM: ROLE
//    @Test
//    @DisplayName("BVA_ROLE_01 – role mặc định → \"PATIENT\"")
//    void role_default_returnsPatient() {
//        assertEquals("PATIENT", patientDTO.getUppercase_role());
//    }
//
//    @Test
//    @DisplayName("BVA_ROLE_02 – role = \"admin\" → \"ADMIN\"")
//    void role_customValue_uppercase() {
//        patientDTO.setRole("admin");
//        assertEquals("ADMIN", patientDTO.getUppercase_role());
//    }
//
//    @Test
//    @DisplayName("BVA_ROLE_03 – role rỗng → \"\"")
//    void role_empty_uppercase() {
//        patientDTO.setRole("");
//        assertEquals("", patientDTO.getUppercase_role());
//    }
//
//    @Test
//    @DisplayName("BVA_ROLE_04 – role null → NullPointerException")
//    void role_null_throwsNPE() {
//        patientDTO.setRole(null);
//        assertThrows(NullPointerException.class, () -> patientDTO.getUppercase_role());
//    }

//    // NHÓM: ID
//    @Test
//    @DisplayName("BVA_ID_01 – id = 0")
//    void id_zero() {
//        patientDTO.setId(0L);
//        assertEquals(0L, patientDTO.getId());
//    }
//
//    @Test
//    @DisplayName("BVA_ID_02 – id = 1")
//    void id_one() {
//        patientDTO.setId(1L);
//        assertEquals(1L, patientDTO.getId());
//    }
//
//    @Test
//    @DisplayName("BVA_ID_03 – id = Long.MAX_VALUE")
//    void id_maxLong() {
//        patientDTO.setId(Long.MAX_VALUE);
//        assertEquals(Long.MAX_VALUE, patientDTO.getId());
//    }
//
//    @Test
//    @DisplayName("BVA_ID_04 – id âm")
//    void id_negative() {
//        patientDTO.setId(-1L);
//        assertEquals(-1L, patientDTO.getId());
//    }
}