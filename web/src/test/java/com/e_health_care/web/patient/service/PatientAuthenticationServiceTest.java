package com.e_health_care.web.patient.service;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.e_health_care.web.patient.dto.PatientDTO;
import com.e_health_care.web.patient.model.Patient;
import com.e_health_care.web.patient.repository.PatientRepository;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;

@ExtendWith(MockitoExtension.class)
@Epic("Patient Management")
@Feature("Patient Authentication")
class PatientAuthenticationServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientDetailsService patientDetailsService;

    @Mock
    private PatientJwtService jwtService;

    private PasswordEncoder passwordEncoder;

    private PatientAuthenticationService patientAuthenticationService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        patientAuthenticationService = new PatientAuthenticationService();
        ReflectionTestUtils.setField(patientAuthenticationService, "patientRepository", patientRepository);
        ReflectionTestUtils.setField(patientAuthenticationService, "patientDetailsService", patientDetailsService);
        ReflectionTestUtils.setField(patientAuthenticationService, "jwtService", jwtService);
        ReflectionTestUtils.setField(patientAuthenticationService, "passwordEncoder", passwordEncoder);
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        PatientDTO patientDTO = buildPatientDto("patient@example.com", "secret123");
        when(patientRepository.findByEmail(patientDTO.getEmail())).thenReturn(Optional.of(new Patient()));

        assertThrows(RuntimeException.class, () -> patientAuthenticationService.register(patientDTO));

        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void registerShouldSaveNewPatient() {
        PatientDTO patientDTO = buildPatientDto("new.patient@example.com", "secret123");
        when(patientRepository.findByEmail(patientDTO.getEmail())).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Patient savedPatient = patientAuthenticationService.register(patientDTO);

        assertNotNull(savedPatient);
        assertEquals(patientDTO.getEmail(), savedPatient.getEmail());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void verifyShouldReturnNullWhenPasswordIsWrong() {
        String email = "patient@example.com";
        String rawPassword = "wrong-password";
        when(patientDetailsService.loadUserByUsername(email)).thenReturn(
                User.withUsername(email)
                        .password(passwordEncoder.encode("correct-password"))
                        .authorities(Collections.emptyList())
                        .build()
        );

        String token = patientAuthenticationService.verify(buildPatientDto(email, rawPassword));

        assertNull(token);
    }

    @Test
    void verifyShouldReturnJwtTokenWhenCredentialsAreCorrect() {
        String email = "patient@example.com";
        String password = "correct-password";
        when(patientDetailsService.loadUserByUsername(email)).thenReturn(
                User.withUsername(email)
                        .password(passwordEncoder.encode(password))
                        .authorities(Collections.emptyList())
                        .build()
        );
        when(jwtService.generateToken(eq(email))).thenReturn("jwt-token");

        String token = patientAuthenticationService.verify(buildPatientDto(email, password));

        assertEquals("jwt-token", token);
        verify(jwtService).generateToken(email);
    }

    private static PatientDTO buildPatientDto(String email, String password) {
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setEmail(email);
        patientDTO.setPassword(password);
        return patientDTO;
    }
}