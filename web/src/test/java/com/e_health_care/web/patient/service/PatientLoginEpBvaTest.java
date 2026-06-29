package com.e_health_care.web.patient.service;

import com.e_health_care.web.patient.dto.PatientDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientLoginEpBvaTest {

    @Mock private PatientDetailsService patientDetailsService;
    @Mock private PatientJwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PatientAuthenticationService service;

    // Hàm hỗ trợ tạo nhanh DTO
    private PatientDTO createDto(String email, String password) {
        PatientDTO dto = new PatientDTO();
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    @Test
    @DisplayName("TC01 [V1, V2, B2, B3]: Thông tin hợp lệ -> Đăng nhập thành công, trả về Token")
    void tc01_validCredentials_shouldReturnToken() {
        PatientDTO dto = createDto("patient@example.com", "password123");
        UserDetails userDetails = User.withUsername(dto.getEmail())
                .password("encodedPassword")
                .authorities(Collections.emptyList())
                .build();

        when(patientDetailsService.loadUserByUsername(dto.getEmail())).thenReturn(userDetails);
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(dto.getEmail())).thenReturn("mock-jwt-token");

        String token = service.verify(dto);
        assertEquals("mock-jwt-token", token);
    }

    @Test
    @DisplayName("TC_Bug_01 [X1]: Email bị rỗng/null -> Bắt buộc ném lỗi Validation")
    void tcBug01_nullEmail_shouldThrowException() {
        PatientDTO dto = createDto(null, "password123");

        Exception ex = assertThrows(RuntimeException.class, () -> service.verify(dto),
                "Lỗi: Hệ thống cho phép email null đi qua mà không ném lỗi RuntimeException!");
        assertTrue(ex.getMessage().contains("Email is required"));
    }

    @Test
    @DisplayName("TC_Bug_02 [X2]: Email sai định dạng (thiếu @) -> Bắt buộc ném lỗi Validation")
    void tcBug02_invalidEmailFormat_shouldThrowException() {
        PatientDTO dto = createDto("invalid-email-format", "password123");

        Exception ex = assertThrows(RuntimeException.class, () -> service.verify(dto),
                "Lỗi: Hệ thống cho phép email sai định dạng đi qua mà không ném lỗi!");
        assertTrue(ex.getMessage().contains("Invalid email format"));
    }

    @Test
    @DisplayName("TC_Bug_03 [X6, B1]: Mật khẩu 7 ký tự (dưới biên 8) -> Bắt buộc ném lỗi Validation")
    void tcBug03_passwordTooShort_shouldThrowException() {
        PatientDTO dto = createDto("patient@example.com", "short12"); // 7 ký tự

        Exception ex = assertThrows(RuntimeException.class, () -> service.verify(dto),
                "Lỗi: Hệ thống cho phép mật khẩu dưới 8 ký tự đi qua mà không chặn!");
        assertTrue(ex.getMessage().contains("Password must be 8-50 characters"));
    }
}