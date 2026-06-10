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
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
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

    // Helper: tạo UserDetails mock đầy đủ tránh LockedException
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

    // UTCID01 – email hợp lệ + password hợp lệ → trả về JWT token (HTTP 200)
    @Test
    @DisplayName("UTCID01 – email valid + password valid → login thành công")
    void utcid01_validEmailAndPassword_returnsToken() {
        DoctorDTO dto = new DoctorDTO();
        dto.setEmail("doctor@test.com");
        dto.setPassword("DoctorPass1");

        UserDetails ud = mockUserDetails(dto.getEmail(), "encodedPassword");
        when(doctorDetailsService.loadUserByUsername(dto.getEmail())).thenReturn(ud);
        when(passwordEncoder.matches(dto.getPassword(), "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(dto.getEmail())).thenReturn("mock.jwt.token");

        String token = doctorAuthenticationService.verify(dto);

        assertNotNull(token, "HTTP 200 – login thành công, trả về token");
        assertEquals("mock.jwt.token", token);
    }

    // UTCID02 – email sai format → trả về null (HTTP 400)
    @Test
    @DisplayName("UTCID02 – email invalid format → login thất bại")
    void utcid02_invalidEmailFormat_returnsNull() {
        DoctorDTO dto = new DoctorDTO();
        dto.setEmail("invalid-email");
        dto.setPassword("DoctorPass1");

        when(doctorDetailsService.loadUserByUsername(dto.getEmail()))
                .thenThrow(new UsernameNotFoundException("invalid email format"));

        assertNull(doctorAuthenticationService.verify(dto), "HTTP 400 – email sai format");
    }

    // UTCID03 – email null → trả về null (HTTP 400)
    @Test
    @DisplayName("UTCID03 – email null → login thất bại")
    void utcid03_nullEmail_returnsNull() {
        DoctorDTO dto = new DoctorDTO();
        dto.setEmail(null);
        dto.setPassword("DoctorPass1");

        lenient().when(doctorDetailsService.loadUserByUsername(any()))
                .thenThrow(new UsernameNotFoundException("email is null"));

        assertNull(doctorAuthenticationService.verify(dto), "HTTP 400 – email null");
    }

    // UTCID04 – password length = 7 (dưới min 8) → trả về null (HTTP 400)
    @Test
    @DisplayName("UTCID04 – password length=7 (dưới min) → login thất bại")
    void utcid04_passwordLength7_returnsNull() {
        DoctorDTO dto = new DoctorDTO();
        dto.setEmail("doctor@test.com");
        dto.setPassword("Pass123"); // 7 ký tự

        assertEquals(7, dto.getPassword().length());

        UserDetails ud = mockUserDetails(dto.getEmail(), "encodedPassword");
        when(doctorDetailsService.loadUserByUsername(dto.getEmail())).thenReturn(ud);
        when(passwordEncoder.matches("Pass123", "encodedPassword")).thenReturn(false);

        assertNull(doctorAuthenticationService.verify(dto), "HTTP 400 – password quá ngắn");
    }

    // UTCID05 – password length = 8 (đúng min) → trả về JWT token (HTTP 200)
    @Test
    @DisplayName("UTCID05 – password length=8 (biên min) → login thành công")
    void utcid05_passwordLength8_returnsToken() {
        DoctorDTO dto = new DoctorDTO();
        dto.setEmail("doctor@test.com");
        dto.setPassword("Pass@123"); // 8 ký tự

        assertEquals(8, dto.getPassword().length());

        UserDetails ud = mockUserDetails(dto.getEmail(), "encodedPassword");
        when(doctorDetailsService.loadUserByUsername(dto.getEmail())).thenReturn(ud);
        when(passwordEncoder.matches("Pass@123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(dto.getEmail())).thenReturn("jwt.token");

        String token = doctorAuthenticationService.verify(dto);

        assertNotNull(token, "HTTP 200 – password đúng biên min, login thành công");
    }
}