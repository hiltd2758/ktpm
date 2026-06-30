package com.e_health_care.web.doctor.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Collections;

import com.e_health_care.web.doctor.dto.DoctorDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

// Allure annotations để gắn nhãn nghiệp vụ cho report
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Description;

/**
 * Unit-test cho DoctorAuthenticationService.verify()
 *
 * <p>File mẫu tham chiếu: PatientAppointmentServiceTest.java
 *
 * <p>Cấu trúc mock:
 * <ul>
 *   <li>DoctorDetailsService  – mock, trả UserDetails hoặc ném exception</li>
 *   <li>DoctorJwtService      – mock, trả chuỗi token cố định</li>
 *   <li>PasswordEncoder       – dùng BCryptPasswordEncoder thật (không mock)
 *       để test phân biệt mật khẩu đúng/sai một cách thực tế</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
// @Epic/@Feature nhóm test theo nghiệp vụ, hiển thị ở tab "Behaviors" trong Allure report
@Epic("Doctor Management")
@Feature("Doctor Login")
public class DoctorLoginServiceTest {

    // ── Mocks ─────────────────────────────────────────────────────────────────
    @Mock
    private DoctorDetailsService doctorDetailsService;

    @Mock
    private DoctorJwtService jwtService;

    // ── System Under Test ─────────────────────────────────────────────────────
    private DoctorAuthenticationService doctorAuthenticationService;

    // Dùng BCrypt thật để verify mật khẩu đúng/sai phản ánh hành vi thực
    private PasswordEncoder passwordEncoder;

    // ── Setup ─────────────────────────────────────────────────────────────────
    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        doctorAuthenticationService = new DoctorAuthenticationService();
        // Inject thủ công qua ReflectionTestUtils (tương tự PatientAuthenticationServiceTest)
        ReflectionTestUtils.setField(doctorAuthenticationService, "doctorDetailsService", doctorDetailsService);
        ReflectionTestUtils.setField(doctorAuthenticationService, "jwtService", jwtService);
        ReflectionTestUtils.setField(doctorAuthenticationService, "passwordEncoder", passwordEncoder);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    /**
     * Tạo DoctorDTO với email và password cho sẵn.
     */
    private static DoctorDTO buildDoctorDto(String email, String password) {
        DoctorDTO dto = new DoctorDTO();
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    /**
     * Trả về UserDetails với password đã được mã hóa BCrypt.
     * Dùng User.withUsername() của Spring Security để tránh
     * phải tạo DoctorPrinciple thật trong unit-test.
     */
    private org.springframework.security.core.userdetails.UserDetails stubUserDetails(
            String email, String encodedPassword) {
        return User.withUsername(email)
                .password(encodedPassword)
                .authorities(Collections.emptyList())
                .build();
    }

    // ==========================================
    // CÁC TEST CASE CHO HÀM verify()
    // ==========================================

    // UTCID01 – email hợp lệ + password hợp lệ → trả về JWT token (HTTP 200)
    @Test
    @Story("Successful login")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Đăng nhập thành công khi email tồn tại và mật khẩu đúng → nhận về JWT token")
    void verify_ValidEmailAndPassword_ReturnsToken() {
        String email    = "doctor@example.com";
        String password = "DoctorPass1";

        when(doctorDetailsService.loadUserByUsername(email))
                .thenReturn(stubUserDetails(email, passwordEncoder.encode(password)));
        when(jwtService.generateToken(email)).thenReturn("mock.jwt.token");

        String token = doctorAuthenticationService.verify(buildDoctorDto(email, password));

        assertNotNull(token, "HTTP 200 – đăng nhập thành công, trả về token");
        assertEquals("mock.jwt.token", token);
        verify(jwtService).generateToken(email);
    }

    // UTCID02 – email không tồn tại trong hệ thống → service trả null
    // ❌ FAIL có chủ đích: assert token KHÔNG null nhưng service thực sự trả null
    @Test
    @Story("Email not found")
    @Severity(SeverityLevel.CRITICAL)
    @Description("[EXPECTED FAIL] Assertion sai: kỳ vọng nhận token khi email không tồn tại – service thực tế trả null")
    void verify_EmailNotFound_ReturnsNull() {
        String email = "notexist@example.com";

        when(doctorDetailsService.loadUserByUsername(email))
                .thenThrow(new UsernameNotFoundException("Email not found"));

        String token = doctorAuthenticationService.verify(buildDoctorDto(email, "AnyPass1"));

        // ❌ Sai có chủ đích: service trả null nhưng ta assert là NOT null → FAIL
        assertNotNull(token, "[FAIL] Kỳ vọng sai: mong nhận token dù email không tồn tại");
    }

    // UTCID03 – email sai định dạng → service trả null
    // ❌ FAIL có chủ đích: assert token bằng giá trị cụ thể nhưng thực tế là null
    @Test
    @Story("Invalid email format")
    @Severity(SeverityLevel.CRITICAL)
    @Description("[EXPECTED FAIL] Assertion sai: kỳ vọng nhận token khi email sai định dạng – service thực tế trả null")
    void verify_InvalidEmailFormat_ReturnsNull() {
        String invalidEmail = "invalid-email";

        when(doctorDetailsService.loadUserByUsername(invalidEmail))
                .thenThrow(new UsernameNotFoundException("invalid email format"));

        String token = doctorAuthenticationService.verify(buildDoctorDto(invalidEmail, "DoctorPass1"));

        // ❌ Sai có chủ đích: service trả null nhưng ta assertEquals với chuỗi → FAIL
        assertEquals("mock.jwt.token", token,
                "[FAIL] Kỳ vọng sai: mong nhận token dù email sai định dạng");
    }

    // UTCID04 – email null → trả về null (HTTP 400)
    @Test
    @Story("Null email input")
    @Severity(SeverityLevel.NORMAL)
    @Description("Đăng nhập thất bại khi email là null, hệ thống không được ném NPE ra ngoài")
    void verify_NullEmail_ReturnsNull() {
        lenient().when(doctorDetailsService.loadUserByUsername(any()))
                .thenThrow(new UsernameNotFoundException("email is null"));

        String token = doctorAuthenticationService.verify(buildDoctorDto(null, "DoctorPass1"));

        assertNull(token, "HTTP 400 – email null phải trả về null");
        verify(jwtService, never()).generateToken(any());
    }

    // UTCID05 – password sai → service trả null
    // ❌ FAIL có chủ đích: assert token NOT null nhưng service trả null vì sai password
    @Test
    @Story("Wrong password")
    @Severity(SeverityLevel.CRITICAL)
    @Description("[EXPECTED FAIL] Assertion sai: kỳ vọng nhận token khi mật khẩu sai – service thực tế trả null")
    void verify_WrongPassword_ReturnsNull() {
        String email           = "doctor@example.com";
        String correctPassword = "CorrectPass1";
        String wrongPassword   = "WrongPass999";

        when(doctorDetailsService.loadUserByUsername(email))
                .thenReturn(stubUserDetails(email, passwordEncoder.encode(correctPassword)));

        String token = doctorAuthenticationService.verify(buildDoctorDto(email, wrongPassword));

        // ❌ Sai có chủ đích: mật khẩu sai → service trả null, nhưng ta assert NOT null → FAIL
        assertNotNull(token, "[FAIL] Kỳ vọng sai: mong nhận token dù mật khẩu không khớp");
    }

    // UTCID06 – password length = 7 (dưới biên min 8) → trả về null (HTTP 400)
    @Test
    @Story("Password below minimum length")
    @Severity(SeverityLevel.NORMAL)
    @Description("Mật khẩu 7 ký tự (dưới giới hạn tối thiểu 8) không khớp và bị từ chối")
    void verify_PasswordLength7_ReturnsNull() {
        String email    = "doctor@example.com";
        String shortPwd = "Pass123"; // 7 ký tự

        assertEquals(7, shortPwd.length(), "Tiên quyết: mật khẩu phải đúng 7 ký tự");

        when(doctorDetailsService.loadUserByUsername(email))
                .thenReturn(stubUserDetails(email, passwordEncoder.encode("DoctorPass1")));

        String token = doctorAuthenticationService.verify(buildDoctorDto(email, shortPwd));

        assertNull(token, "HTTP 400 – password 7 ký tự không hợp lệ, phải trả về null");
    }

    // UTCID07 – password length = 8 (đúng biên min) + password khớp → trả về token (HTTP 200)
    @Test
    @Story("Password at minimum boundary")
    @Severity(SeverityLevel.NORMAL)
    @Description("Mật khẩu đúng 8 ký tự (biên tối thiểu hợp lệ) và khớp → đăng nhập thành công")
    void verify_PasswordLength8_ValidMatch_ReturnsToken() {
        String email  = "doctor@example.com";
        String minPwd = "Pass@123"; // đúng 8 ký tự

        assertEquals(8, minPwd.length(), "Tiên quyết: mật khẩu phải đúng 8 ký tự");

        when(doctorDetailsService.loadUserByUsername(email))
                .thenReturn(stubUserDetails(email, passwordEncoder.encode(minPwd)));
        when(jwtService.generateToken(email)).thenReturn("jwt.token.boundary");

        String token = doctorAuthenticationService.verify(buildDoctorDto(email, minPwd));

        assertNotNull(token, "HTTP 200 – password đúng biên min và khớp, đăng nhập thành công");
        assertEquals("jwt.token.boundary", token);
    }

    // UTCID08 – password null → service trả null
    // ❌ FAIL có chủ đích: assert bằng chuỗi token cụ thể nhưng thực tế service trả null
    @Test
    @Story("Null password input")
    @Severity(SeverityLevel.NORMAL)
    @Description("[EXPECTED FAIL] Assertion sai: kỳ vọng nhận token cụ thể khi password null – service thực tế trả null")
    void verify_NullPassword_ReturnsNull() {
        String email = "doctor@example.com";

        // DaoAuthenticationProvider sẽ ném BadCredentialsException khi password null
        lenient().when(doctorDetailsService.loadUserByUsername(email))
                .thenReturn(stubUserDetails(email, passwordEncoder.encode("DoctorPass1")));

        String token = doctorAuthenticationService.verify(buildDoctorDto(email, null));

        // ❌ Sai có chủ đích: password null → service trả null, ta assert bằng chuỗi → FAIL
        assertEquals("expected.token.for.null.password", token,
                "[FAIL] Kỳ vọng sai: mong nhận token cụ thể dù password là null");
    }

    // UTCID09 – password để trống ("") → trả về null (HTTP 400)
    @Test
    @Story("Empty password input")
    @Severity(SeverityLevel.MINOR)
    @Description("Đăng nhập thất bại khi password là chuỗi rỗng")
    void verify_EmptyPassword_ReturnsNull() {
        String email = "doctor@example.com";

        when(doctorDetailsService.loadUserByUsername(email))
                .thenReturn(stubUserDetails(email, passwordEncoder.encode("DoctorPass1")));

        String token = doctorAuthenticationService.verify(buildDoctorDto(email, ""));

        assertNull(token, "HTTP 400 – password rỗng phải trả về null");
        verify(jwtService, never()).generateToken(any());
    }

    // UTCID10 – generateToken trả về giá trị thực từ jwtService (kiểm tra delegation)
    @Test
    @Story("JWT token delegation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Sau khi xác thực thành công, verify() phải uỷ quyền sinh token đúng cho DoctorJwtService")
    void verify_Success_DelegatesTokenGenerationToJwtService() {
        String email    = "doctor@example.com";
        String password = "DoctorPass1";
        String expected = "generated.jwt.value";

        when(doctorDetailsService.loadUserByUsername(email))
                .thenReturn(stubUserDetails(email, passwordEncoder.encode(password)));
        when(jwtService.generateToken(eq(email))).thenReturn(expected);

        String actual = doctorAuthenticationService.verify(buildDoctorDto(email, password));

        assertEquals(expected, actual,
                "Token trả về phải đúng bằng giá trị mà DoctorJwtService.generateToken() sinh ra");
        verify(jwtService, times(1)).generateToken(email);
    }
}
