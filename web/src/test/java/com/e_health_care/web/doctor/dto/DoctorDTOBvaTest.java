package com.e_health_care.web.doctor.dto;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.e_health_care.web.testsupport.BvaValidationHelper;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Epic("Doctor Management")
@Feature("Doctor DTO Validation")
class DoctorDTOBvaTest {

    @Test
    @DisplayName("All nominal values should be valid")
    @Story("Nominal valid data")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Kiểm tra toàn bộ dữ liệu hợp lệ phải vượt qua validation")
    void allNominal_shouldBeValid() {
        DoctorDTO doctorDTO = buildValidDoctorDto();

        assertTrue(BvaValidationHelper.isValidDoctorDto(doctorDTO));
    }

    @Test
    @DisplayName("Short nominal email should be valid")
    @Story("Valid short email")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra email hợp lệ với độ dài ngắn vẫn phải vượt qua validation")
    void email_nominalShort_isValid() {
        DoctorDTO doctorDTO = buildValidDoctorDto();
        doctorDTO.setEmail("a@b.com");

        assertTrue(BvaValidationHelper.isValidDoctorDto(doctorDTO));
    }

    @Test
    @DisplayName("Long nominal email should be valid")
    @Story("Valid long email")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra email hợp lệ với độ dài lớn vẫn phải vượt qua validation")
    void email_nominalLong_isValid() {
        DoctorDTO doctorDTO = buildValidDoctorDto();
        doctorDTO.setEmail(repeat('a', 40) + "@b.com");

        assertTrue(BvaValidationHelper.isValidDoctorDto(doctorDTO));
    }

    @ParameterizedTest(name = "email boundary: {0}")
    @MethodSource("emailCases")
    @DisplayName("Email boundary values")
    @Story("Email boundary validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra các giá trị biên của trường email")
    void emailBoundaryValues(String email, boolean expectedValid) {
        DoctorDTO doctorDTO = buildValidDoctorDto();
        doctorDTO.setEmail(email);

        assertEquals(expectedValid, BvaValidationHelper.isValidDoctorDto(doctorDTO));
    }

    @ParameterizedTest(name = "password boundary: {0}")
    @MethodSource("passwordCases")
    @DisplayName("Password boundary values")
    @Story("Password boundary validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra các giá trị biên của trường password")
    void passwordBoundaryValues(String password, boolean expectedValid) {
        DoctorDTO doctorDTO = buildValidDoctorDto();
        doctorDTO.setPassword(password);

        assertEquals(expectedValid, BvaValidationHelper.isValidDoctorDto(doctorDTO));
    }

    @ParameterizedTest(name = "first name boundary: {0}")
    @MethodSource("firstNameCases")
    @DisplayName("First name boundary values")
    @Story("First name boundary validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Kiểm tra các giá trị biên của trường firstName")
    void firstNameBoundaryValues(String firstName, boolean expectedValid) {
        DoctorDTO doctorDTO = buildValidDoctorDto();
        doctorDTO.setFirstName(firstName);

        assertEquals(expectedValid, BvaValidationHelper.isValidDoctorDto(doctorDTO));
    }

    @ParameterizedTest(name = "phone boundary: {0}")
    @MethodSource("phoneCases")
    @DisplayName("Phone boundary values")
    @Story("Phone boundary validation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Kiểm tra các giá trị biên của trường số điện thoại")
    void phoneBoundaryValues(String phone, boolean expectedValid) {
        DoctorDTO doctorDTO = buildValidDoctorDto();
        doctorDTO.setPhone(phone);

        assertEquals(expectedValid, BvaValidationHelper.isValidDoctorDto(doctorDTO));
    }

    Stream<Arguments> emailCases() {
        return Stream.of(
                Arguments.of(null, false),
                Arguments.of("a@b.c", true),
                Arguments.of("ab.c", false)
        );
    }

    Stream<Arguments> passwordCases() {
        return Stream.of(
                Arguments.of("1234567", false),
                Arguments.of("12345678", true),
                Arguments.of(repeat('a', 50), true),
                Arguments.of(repeat('b', 51), false)
        );
    }

    Stream<Arguments> firstNameCases() {
        return Stream.of(
                Arguments.of("", false),
                Arguments.of("A", true),
                Arguments.of(repeat('x', 50), true),
                Arguments.of(repeat('y', 51), false)
        );
    }

    Stream<Arguments> phoneCases() {
        return Stream.of(
                Arguments.of("012345678", false),
                Arguments.of("0123456789", true),
                Arguments.of("01234567890", false)
        );
    }

    private static DoctorDTO buildValidDoctorDto() {
        DoctorDTO doctorDTO = new DoctorDTO();
        doctorDTO.setEmail("doctor@example.com");
        doctorDTO.setPassword("password123");
        doctorDTO.setFirstName("Doctor");
        doctorDTO.setPhone("0123456789");
        doctorDTO.setLastName("Example");
        doctorDTO.setAddress("123 Main Street");
        doctorDTO.setField("Cardiology");
        return doctorDTO;
    }

    private static String repeat(char character, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int index = 0; index < count; index++) {
            builder.append(character);
        }
        return builder.toString();
    }
}
