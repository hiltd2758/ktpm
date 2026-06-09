package com.e_health_care.web.doctor.service;

import com.e_health_care.web.doctor.dto.DoctorDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorAuthenticationServiceTest {

    @Mock
    private DoctorDetailsService doctorDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DoctorJwtService jwtService;

    @InjectMocks
    private DoctorAuthenticationService doctorAuthenticationService;

    private DoctorDTO validDoctor;

    private UserDetails mockUserDetails(String email, String encodedPassword) {
        UserDetails ud = mock(UserDetails.class);
        lenient().when(ud.getUsername()).thenReturn(email);
        lenient().when(ud.getPassword()).thenReturn(encodedPassword);
        lenient().when(ud.isAccountNonLocked()).thenReturn(true);
        lenient().when(ud.isAccountNonExpired()).thenReturn(true);
        lenient().when(ud.isCredentialsNonExpired()).thenReturn(true);
        lenient().when(ud.isEnabled()).thenReturn(true);
        return ud;
    }

    @BeforeEach
    void setUp() {
        validDoctor = new DoctorDTO();
        validDoctor.setEmail("doctor@hospital.com");
        validDoctor.setPassword("SecurePass@123");
    }

    // ══════════════════════════════════════════════════════
    // PHẦN 1: UNIT TEST (3 test case theo yêu cầu)
    // ══════════════════════════════════════════════════════

    @Test
    @DisplayName("TC01 – Đăng nhập thành công → trả về JWT token")
    void verify_validCredentials_returnsJwtToken() {
        UserDetails ud = mockUserDetails(validDoctor.getEmail(), "encodedPassword");
        when(doctorDetailsService.loadUserByUsername(validDoctor.getEmail())).thenReturn(ud);
        when(passwordEncoder.matches(validDoctor.getPassword(), "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(validDoctor.getEmail())).thenReturn("mock.jwt.token");

        String token = doctorAuthenticationService.verify(validDoctor);

        assertNotNull(token);
        assertEquals("mock.jwt.token", token);
    }

    @Test
    @DisplayName("TC02 – Email không tồn tại → verify() trả về null")
    void verify_emailNotFound_returnsNull() {
        when(doctorDetailsService.loadUserByUsername(anyString()))
                .thenThrow(new UsernameNotFoundException("Email not found"));

        assertNull(doctorAuthenticationService.verify(validDoctor));
    }

    @Test
    @DisplayName("TC03 – Password sai → verify() trả về null")
    void verify_wrongPassword_returnsNull() {
        UserDetails ud = mockUserDetails(validDoctor.getEmail(), "encodedPassword");
        when(doctorDetailsService.loadUserByUsername(validDoctor.getEmail())).thenReturn(ud);
        when(passwordEncoder.matches(validDoctor.getPassword(), "encodedPassword")).thenReturn(false);

        assertNull(doctorAuthenticationService.verify(validDoctor));
    }

    // ── Dư – comment out ──

//    @Test
//    @DisplayName("TC04 – Email null → verify() trả về null an toàn")
//    void verify_nullEmail_returnsNull() {
//        DoctorDTO dto = new DoctorDTO();
//        dto.setEmail(null);
//        dto.setPassword("SomePass");
//        when(doctorDetailsService.loadUserByUsername(any()))
//                .thenThrow(new UsernameNotFoundException("null email"));
//        assertNull(doctorAuthenticationService.verify(dto));
//    }

//    @Test
//    @DisplayName("TC05 – Password null → verify() trả về null")
//    void verify_nullPassword_returnsNull() {
//        DoctorDTO dto = new DoctorDTO();
//        dto.setEmail("doctor@hospital.com");
//        dto.setPassword(null);
//        UserDetails ud = mockUserDetails(dto.getEmail(), "encodedPassword");
//        when(doctorDetailsService.loadUserByUsername(dto.getEmail())).thenReturn(ud);
//        lenient().when(passwordEncoder.matches(null, "encodedPassword")).thenReturn(false);
//        assertNull(doctorAuthenticationService.verify(dto));
//    }

//    @Test
//    @DisplayName("TC06 – getUppercase_role() mặc định → \"DOCTOR\"")
//    void getUppercaseRole_defaultRole_returnsDOCTOR() {
//        assertEquals("DOCTOR", new DoctorDTO().getUppercase_role());
//    }

//    @Test
//    @DisplayName("TC07 – Thành công → generateToken() gọi đúng 1 lần")
//    void verify_success_callsGenerateTokenOnce() {
//        UserDetails ud = mockUserDetails(validDoctor.getEmail(), "encoded");
//        when(doctorDetailsService.loadUserByUsername(validDoctor.getEmail())).thenReturn(ud);
//        when(passwordEncoder.matches(validDoctor.getPassword(), "encoded")).thenReturn(true);
//        when(jwtService.generateToken(validDoctor.getEmail())).thenReturn("jwt");
//        doctorAuthenticationService.verify(validDoctor);
//        verify(jwtService, times(1)).generateToken(validDoctor.getEmail());
//    }

//    @Test
//    @DisplayName("TC08 – Thất bại → generateToken() không được gọi")
//    void verify_failure_neverCallsGenerateToken() {
//        when(doctorDetailsService.loadUserByUsername(anyString()))
//                .thenThrow(new UsernameNotFoundException("not found"));
//        doctorAuthenticationService.verify(validDoctor);
//        verify(jwtService, never()).generateToken(anyString());
//    }

    // ══════════════════════════════════════════════════════
    // PHẦN 2: BVA – Dư, comment out
    // ══════════════════════════════════════════════════════

//    @Test
//    @DisplayName("BVA_EMAIL_01 – Email rỗng → verify() trả về null")
//    void bva_email_empty_returnsNull() { ... }

//    @Test
//    @DisplayName("BVA_EMAIL_02 – Email 1 ký tự → verify() trả về null")
//    void bva_email_oneChar_returnsNull() { ... }

//    @Test
//    @DisplayName("BVA_EMAIL_03 – Email hợp lệ tối thiểu (a@b.c)")
//    void bva_email_minimalValid_processed() { ... }

//    @Test
//    @DisplayName("BVA_EMAIL_04 – Email 254 ký tự")
//    void bva_email_254chars_processed() { ... }

//    @Test
//    @DisplayName("BVA_EMAIL_05 – Email 255 ký tự")
//    void bva_email_255chars_processed() { ... }

//    @Test
//    @DisplayName("BVA_PASS_01 – Password rỗng → verify() trả về null")
//    void bva_password_empty_returnsNull() { ... }

//    @Test
//    @DisplayName("BVA_PASS_02 – Password 7 ký tự")
//    void bva_password_sevenChars_returnsNull() { ... }

//    @Test
//    @DisplayName("BVA_PASS_03 – Password 8 ký tự")
//    void bva_password_eightChars_success() { ... }

//    @Test
//    @DisplayName("BVA_PASS_04 – Password 128 ký tự")
//    void bva_password_128chars_success() { ... }

//    @Test
//    @DisplayName("BVA_PASS_05 – Password 129 ký tự")
//    void bva_password_129chars_returnsNull() { ... }

//    @Test
//    @DisplayName("BVA_ROLE_01 – role mặc định")
//    void bva_role_default() { ... }

//    @Test
//    @DisplayName("BVA_ROLE_02 – role rỗng")
//    void bva_role_empty() { ... }

//    @Test
//    @DisplayName("BVA_ROLE_03 – role null → NullPointerException")
//    void bva_role_null_throwsNPE() { ... }
}