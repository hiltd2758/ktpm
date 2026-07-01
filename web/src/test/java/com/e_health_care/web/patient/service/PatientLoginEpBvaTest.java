package com.e_health_care.web.patient.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import com.e_health_care.web.patient.dto.PatientDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Spring Security Imports cần thiết cho việc mock dữ liệu login
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.InternalAuthenticationServiceException; // Import thêm lỗi này

// Allure annotations
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Description;

@ExtendWith(MockitoExtension.class)
@Epic("Patient Management")
@Feature("Patient Authentication")
public class PatientLoginEpBvaTest {

    @Mock
    private PatientDetailsService patientDetailsService;

    @Mock
    private PatientJwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PatientAuthenticationService patientAuthenticationService;

    private PatientDTO validPatientDTO;

    @BeforeEach
    void setUp() {
        validPatientDTO = new PatientDTO();
        validPatientDTO.setEmail("patient@example.com");
        validPatientDTO.setPassword("password123");
    }

    @Test
    @Story("Successful login")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Đăng nhập thành công khi thông tin hợp lệ")
    void tc01_LoginSuccess() {
        UserDetails mockUser = new User(validPatientDTO.getEmail(), "encodedPassword123", Collections.emptyList());

        when(patientDetailsService.loadUserByUsername(validPatientDTO.getEmail())).thenReturn(mockUser);
        when(passwordEncoder.matches(validPatientDTO.getPassword(), "encodedPassword123")).thenReturn(true);
        when(jwtService.generateToken(validPatientDTO.getEmail())).thenReturn("mocked-jwt-token");

        assertDoesNotThrow(() -> {
            String token = patientAuthenticationService.verify(validPatientDTO);
            assertNotNull(token);
        });
    }

    @Test
    @Story("Validation: Email blank")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ném lỗi khi email bị rỗng")
    void tcBug01_EmailBlank_ShouldThrowException() {
        PatientDTO invalidDto = new PatientDTO();
        invalidDto.setEmail("");
        invalidDto.setPassword("password123");

        // Đã sửa thành InternalAuthenticationServiceException để khớp với Spring Security
        assertThrows(InternalAuthenticationServiceException.class, () -> patientAuthenticationService.verify(invalidDto));
    }

    @Test
    @Story("Validation: Invalid email format")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ném lỗi khi email sai định dạng")
    void tcBug02_EmailInvalid_ShouldThrowException() {
        PatientDTO invalidDto = new PatientDTO();
        invalidDto.setEmail("invalid-email");
        invalidDto.setPassword("password123");

        assertThrows(InternalAuthenticationServiceException.class, () -> patientAuthenticationService.verify(invalidDto));
    }

    @Test
    @Story("Validation: Password too short")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ném lỗi khi mật khẩu ngắn hơn 6 ký tự")
    void tcBug03_PassTooShort_ShouldThrowException() {
        PatientDTO invalidDto = new PatientDTO();
        invalidDto.setEmail("patient@example.com");
        invalidDto.setPassword("short");

        assertThrows(InternalAuthenticationServiceException.class, () -> patientAuthenticationService.verify(invalidDto));
    }
}